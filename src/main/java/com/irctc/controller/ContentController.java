package com.irctc.controller;

import com.irctc.dao.ContentDAO;
import com.irctc.dao.DashboardDAO;
import com.irctc.dao.TrainDAO;
import com.irctc.exception.DataAccessException;
import com.irctc.exception.ValidationException;
import com.irctc.model.Announcement;
import com.irctc.model.BannerContent;
import com.irctc.model.QuickAction;
import com.irctc.model.ServiceType;
import com.irctc.model.Station;
import com.irctc.service.ContentService;
import com.irctc.service.DashboardService;
import com.irctc.util.Catalog;
import com.irctc.util.SessionKeys;
import com.irctc.util.Web;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Forms for banner text, shortcuts, class labels, announcements, and station names. */
public class ContentController extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(ContentController.class.getName());
    private final ContentService contentService = new ContentService();
    private final ContentDAO contentDAO = new ContentDAO();
    private final DashboardDAO dashboardDAO = new DashboardDAO();
    private final DashboardService dashboardService = new DashboardService();
    private final TrainDAO trainDAO = new TrainDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            switch (Web.path(request)) {
                case "/manage" -> page(request, response, "manage", "Website & Dashboard Management");
                case "/manage/banner" -> banner(request, response, contentService.banner(), Map.of());
                case "/manage/actions" -> actions(request, response, actionForm(request), Map.of());
                case "/manage/classes" -> classes(request, response);
                case "/manage/announcements" -> announcements(request, response, announcementForm(request), Map.of());
                case "/manage/statistics" -> statistics(request, response);
                case "/manage/routes" -> routes(request, response, null, Map.of());
                default -> response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (DataAccessException ex) {
            LOGGER.log(Level.SEVERE, "Content page failed", ex);
            fail(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            switch (Web.path(request)) {
                case "/manage/banner" -> saveBanner(request, response);
                case "/manage/actions/save" -> saveAction(request, response);
                case "/manage/actions/delete" -> deleteAction(request, response);
                case "/manage/classes/save" -> saveClass(request, response);
                case "/manage/announcements/save" -> saveAnnouncement(request, response);
                case "/manage/announcements/delete" -> deleteAnnouncement(request, response);
                case "/manage/routes/station" -> saveStation(request, response);
                default -> response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (DataAccessException ex) {
            LOGGER.log(Level.SEVERE, "Content save failed", ex);
            Web.flash(request, SessionKeys.FLASH_ERROR, "The change could not be saved.");
            Web.redirect(request, response, "/manage");
        }
    }

    private void saveBanner(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        BannerContent banner = readBanner(request);
        try {
            Part upload = null;
            String type = request.getContentType();
            if (type != null && type.toLowerCase().startsWith("multipart/")) {
                upload = request.getPart("bannerFile");
            }
            contentService.saveBanner(Web.adminId(request), banner, upload);
            Web.flash(request, SessionKeys.FLASH_SUCCESS, "Banner updated. Reload the dashboard to see it.");
            Web.redirect(request, response, "/manage/banner");
        } catch (ValidationException ex) {
            banner(request, response, banner, ex.getFieldErrors());
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Banner image could not be stored", ex);
            Web.flash(request, SessionKeys.FLASH_ERROR, "The image could not be stored.");
            Web.redirect(request, response, "/manage/banner");
        }
    }

    private void saveAction(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        QuickAction action = readAction(request);
        try {
            contentService.saveAction(Web.adminId(request), action);
            Web.flash(request, SessionKeys.FLASH_SUCCESS, "Quick action saved.");
            Web.redirect(request, response, "/manage/actions");
        } catch (ValidationException ex) {
            actions(request, response, action, ex.getFieldErrors());
        }
    }

    private void deleteAction(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Long id = Web.longParam(request, "id");
        if (id != null) {
            contentService.deleteAction(Web.adminId(request), id);
            Web.flash(request, SessionKeys.FLASH_SUCCESS, "Quick action removed.");
        }
        Web.redirect(request, response, "/manage/actions");
    }

    private void saveClass(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ServiceType travelClass = new ServiceType();
        travelClass.setCode(Web.param(request, "code"));
        travelClass.setName(Web.param(request, "displayName"));
        travelClass.setColor(Web.param(request, "color"));
        travelClass.setOffered(request.getParameter("visible") != null);
        travelClass.setSortOrder(Web.intParam(request, "sortOrder", 0));
        try {
            contentService.saveClass(Web.adminId(request), travelClass);
            Web.flash(request, SessionKeys.FLASH_SUCCESS, "Class label updated.");
        } catch (ValidationException ex) {
            Web.flash(request, SessionKeys.FLASH_ERROR, ex.getFieldErrors().values().stream().findFirst().orElse("Check the class form."));
        }
        Web.redirect(request, response, "/manage/classes");
    }

    private void saveAnnouncement(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Announcement announcement = readAnnouncement(request);
        try {
            contentService.saveAnnouncement(Web.adminId(request), announcement);
            Web.flash(request, SessionKeys.FLASH_SUCCESS, "Announcement saved.");
            Web.redirect(request, response, "/manage/announcements");
        } catch (ValidationException ex) {
            announcements(request, response, announcement, ex.getFieldErrors());
        }
    }

    private void deleteAnnouncement(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Long id = Web.longParam(request, "id");
        if (id != null) {
            contentService.deleteAnnouncement(Web.adminId(request), id);
            Web.flash(request, SessionKeys.FLASH_SUCCESS, "Announcement removed.");
        }
        Web.redirect(request, response, "/manage/announcements");
    }

    private void saveStation(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Station station = new Station();
        Long id = Web.longParam(request, "stationId");
        station.setId(id == null ? 0 : id);
        station.setStationName(Web.param(request, "stationName"));
        station.setCity(Web.param(request, "city"));
        station.setStateName(Web.param(request, "stateName"));
        try {
            contentService.saveStation(Web.adminId(request), station);
            Web.flash(request, SessionKeys.FLASH_SUCCESS, "Station name updated.");
            Web.redirect(request, response, "/manage/routes");
        } catch (ValidationException ex) {
            routes(request, response, station, ex.getFieldErrors());
        }
    }

    private void banner(HttpServletRequest request, HttpServletResponse response, BannerContent banner, Map<String, String> errors)
            throws ServletException, IOException {
        request.setAttribute("banner", banner);
        request.setAttribute("errors", errors);
        page(request, response, "manage-banner", "Welcome banner");
    }

    private void actions(HttpServletRequest request, HttpServletResponse response, QuickAction form, Map<String, String> errors)
            throws ServletException, IOException {
        request.setAttribute("actions", contentDAO.actions(false));
        request.setAttribute("form", form);
        request.setAttribute("icons", Catalog.ACTION_ICONS);
        request.setAttribute("errors", errors);
        page(request, response, "manage-actions", "Quick actions");
    }

    private void classes(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setAttribute("classes", contentDAO.classes());
        page(request, response, "manage-classes", "Booking classes");
    }

    private void announcements(HttpServletRequest request, HttpServletResponse response, Announcement form, Map<String, String> errors)
            throws ServletException, IOException {
        request.setAttribute("announcements", contentDAO.announcements());
        request.setAttribute("form", form);
        request.setAttribute("errors", errors);
        page(request, response, "manage-announcements", "Announcements");
    }

    private void statistics(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setAttribute("metrics", dashboardService.load().getMetrics());
        page(request, response, "manage-statistics", "Booking statistics");
    }

    private void routes(HttpServletRequest request, HttpServletResponse response, Station form, Map<String, String> errors)
            throws ServletException, IOException {
        if (form == null) {
            Long stationId = Web.longParam(request, "stationId");
            if (stationId != null) {
                form = trainDAO.findStation(stationId);
            }
        }
        if (form == null) {
            form = new Station();
        }
        request.setAttribute("routes", dashboardDAO.topRoutes(20));
        request.setAttribute("stations", trainDAO.stations());
        request.setAttribute("stationForm", form);
        request.setAttribute("errors", errors);
        page(request, response, "manage-routes", "Routes");
    }

    private void page(HttpServletRequest request, HttpServletResponse response, String view, String title)
            throws ServletException, IOException {
        request.setAttribute("pageTitle", title);
        Web.forward(request, response, view);
    }

    private void fail(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setAttribute("errorTitle", "Content unavailable");
        request.setAttribute("errorMessage", "The management page could not be loaded.");
        Web.forward(request, response, "error");
    }

    private BannerContent readBanner(HttpServletRequest request) {
        BannerContent banner = new BannerContent();
        banner.setTitle(Web.param(request, "bannerTitle"));
        banner.setDescription(Web.param(request, "bannerDescription"));
        banner.setBadge(Web.param(request, "bannerBadge"));
        banner.setImage(Web.param(request, "bannerImage"));
        banner.setActivityLimit(Web.intParam(request, "activityLimit", 5));
        return banner;
    }

    private QuickAction readAction(HttpServletRequest request) {
        QuickAction action = new QuickAction();
        Long id = Web.longParam(request, "id");
        action.setId(id == null ? 0 : id);
        action.setLabel(Web.param(request, "label"));
        action.setIconName(Web.param(request, "iconName"));
        action.setDestination(Web.param(request, "destination"));
        action.setVisible(request.getParameter("visible") != null);
        action.setSortOrder(Web.intParam(request, "sortOrder", 1));
        return action;
    }

    private QuickAction actionForm(HttpServletRequest request) {
        Long id = Web.longParam(request, "id");
        if (id != null) {
            QuickAction existing = contentDAO.findAction(id);
            if (existing != null) {
                return existing;
            }
        }
        QuickAction blank = new QuickAction();
        blank.setIconName("ticket");
        blank.setVisible(true);
        blank.setSortOrder(1);
        blank.setDestination("/bookings");
        return blank;
    }

    private Announcement readAnnouncement(HttpServletRequest request) {
        Announcement announcement = new Announcement();
        Long id = Web.longParam(request, "id");
        announcement.setId(id == null ? 0 : id);
        announcement.setTitle(Web.param(request, "title"));
        announcement.setMessage(Web.param(request, "message"));
        announcement.setActive(request.getParameter("active") != null);
        return announcement;
    }

    private Announcement announcementForm(HttpServletRequest request) {
        Long id = Web.longParam(request, "id");
        if (id == null) {
            Announcement blank = new Announcement();
            blank.setActive(true);
            return blank;
        }
        Announcement existing = contentDAO.findAnnouncement(id);
        if (existing != null) {
            return existing;
        }
        Announcement blank = new Announcement();
        blank.setActive(true);
        return blank;
    }
}
