package com.irctc.service;

import com.irctc.dao.ContentDAO;
import com.irctc.dao.SettingsDAO;
import com.irctc.dao.TrainDAO;
import com.irctc.exception.ValidationException;
import com.irctc.model.Announcement;
import com.irctc.model.BannerContent;
import com.irctc.model.QuickAction;
import com.irctc.model.ServiceType;
import com.irctc.model.Station;
import com.irctc.util.Catalog;
import com.irctc.util.DaoSupport;
import com.irctc.util.UploadStore;
import com.irctc.util.Validators;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/** Checks dashboard content, then saves it with the matching activity row. */
public class ContentService {
    private final SettingsDAO settingsDAO = new SettingsDAO();
    private final ContentDAO contentDAO = new ContentDAO();
    private final TrainDAO trainDAO = new TrainDAO();
    private final ActivityService activityService = new ActivityService();

    public BannerContent banner() {
        Map<String, String> settings = settingsDAO.findAll();
        BannerContent banner = new BannerContent();
        banner.setTitle(orDefault(settings.get("banner_title"), "Welcome Back"));
        banner.setDescription(orDefault(settings.get("banner_description"), ""));
        banner.setBadge(orDefault(settings.get("banner_badge"), "Employee Dashboard"));
        banner.setImage(orDefault(settings.get("banner_image"), "/assets/images/train-banner.jpg"));
        banner.setActivityLimit(activityLimit(settings.get("dashboard_activity_limit")));
        return banner;
    }

    public List<QuickAction> visibleActions() {
        return contentDAO.actions(true);
    }

    public List<QuickAction> allActions() {
        return contentDAO.actions(false);
    }

    public List<Announcement> activeAnnouncements() {
        return contentDAO.activeAnnouncements();
    }

    public void saveBanner(long adminId, BannerContent banner, Part upload) throws ValidationException, IOException {
        Validators validators = new Validators();
        validators.required("bannerTitle", banner.getTitle(), "Banner title");
        validators.maxLength("bannerTitle", banner.getTitle(), 80, "Banner title");
        validators.required("bannerDescription", banner.getDescription(), "Banner description");
        validators.maxLength("bannerDescription", banner.getDescription(), 240, "Banner description");
        validators.required("bannerBadge", banner.getBadge(), "Banner badge");
        validators.maxLength("bannerBadge", banner.getBadge(), 60, "Banner badge");
        if (upload != null && upload.getSize() > 0) {
            String stored = storeUpload(upload, validators);
            if (stored != null) {
                banner.setImage("/media/" + stored);
            }
        }
        if (!Catalog.safeImageReference(banner.getImage())) {
            validators.add("bannerImage", "Use an uploaded image, a project image path, or an https address.");
        }
        if (banner.getActivityLimit() != 3 && banner.getActivityLimit() != 5 && banner.getActivityLimit() != 8) {
            validators.add("activityLimit", "Choose 3, 5, or 8 activities.");
        }
        validators.finish();
        DaoSupport.transaction("The banner could not be saved.", connection -> {
            settingsDAO.upsert(connection, "banner_title", banner.getTitle().trim());
            settingsDAO.upsert(connection, "banner_description", banner.getDescription().trim());
            settingsDAO.upsert(connection, "banner_badge", banner.getBadge().trim());
            settingsDAO.upsert(connection, "banner_image", banner.getImage().trim());
            settingsDAO.upsert(connection, "dashboard_activity_limit", Integer.toString(banner.getActivityLimit()));
            activityService.record(connection, adminId, "CONTENT_UPDATED", "Welcome banner updated", "BANNER", null);
        });
    }

    public void saveAction(long adminId, QuickAction action) throws ValidationException {
        Validators validators = new Validators();
        validators.required("label", action.getLabel(), "Label");
        validators.maxLength("label", action.getLabel(), 80, "Label");
        if (!Catalog.ACTION_ICONS.contains(action.getIconName())) {
            validators.add("iconName", "Choose one of the listed icons.");
        }
        if (!Catalog.safeDestination(action.getDestination())) {
            validators.add("destination", "Destination must be a page inside this portal, such as /bookings.");
        }
        if (action.getSortOrder() < 1 || action.getSortOrder() > 50) {
            validators.add("sortOrder", "Sort order must be from 1 to 50.");
        }
        validators.finish();
        action.setLabel(action.getLabel().trim());
        action.setDestination(action.getDestination().trim());
        DaoSupport.transaction("The quick action could not be saved.", connection -> {
            long id = contentDAO.saveAction(connection, action);
            activityService.record(connection, adminId, "CONTENT_UPDATED", "Quick action saved: " + action.getLabel(), "ACTION", id);
        });
    }

    public void deleteAction(long adminId, long id) {
        QuickAction existing = contentDAO.findAction(id);
        if (existing == null) {
            return;
        }
        DaoSupport.transaction("The quick action could not be removed.", connection -> {
            contentDAO.deleteAction(connection, id);
            activityService.record(connection, adminId, "CONTENT_UPDATED", "Quick action removed: " + existing.getLabel(), "ACTION", id);
        });
    }

    public void saveClass(long adminId, ServiceType travelClass) throws ValidationException {
        if (!Catalog.TRAVEL_CLASSES.contains(travelClass.getCode())) {
            Validators validators = new Validators();
            validators.add("code", "That class is not used by bookings.");
            validators.finish();
        }
        Validators validators = new Validators();
        validators.required("displayName", travelClass.getName(), "Display name");
        validators.maxLength("displayName", travelClass.getName(), 80, "Display name");
        if (travelClass.getColor() == null || !travelClass.getColor().matches("#[0-9A-Fa-f]{6}")) {
            validators.add("color", "Use a color such as #2563eb.");
        }
        if (travelClass.getSortOrder() < 1 || travelClass.getSortOrder() > 20) {
            validators.add("sortOrder", "Sort order must be from 1 to 20.");
        }
        validators.finish();
        travelClass.setName(travelClass.getName().trim());
        DaoSupport.transaction("The class label could not be saved.", connection -> {
            contentDAO.updateClass(connection, travelClass);
            activityService.record(connection, adminId, "CONTENT_UPDATED",
                    "Booking class updated: " + travelClass.getCode(), "CLASS", null);
        });
    }

    public void saveAnnouncement(long adminId, Announcement announcement) throws ValidationException {
        Validators validators = new Validators();
        validators.required("title", announcement.getTitle(), "Title");
        validators.maxLength("title", announcement.getTitle(), 120, "Title");
        validators.required("message", announcement.getMessage(), "Message");
        validators.maxLength("message", announcement.getMessage(), 500, "Message");
        validators.finish();
        announcement.setTitle(announcement.getTitle().trim());
        announcement.setMessage(announcement.getMessage().trim());
        DaoSupport.transaction("The announcement could not be saved.", connection -> {
            contentDAO.saveAnnouncement(connection, announcement);
            activityService.record(connection, adminId, "CONTENT_UPDATED",
                    "Announcement saved: " + announcement.getTitle(), "ANNOUNCEMENT", announcement.getId() > 0 ? announcement.getId() : null);
        });
    }

    public void deleteAnnouncement(long adminId, long id) {
        Announcement existing = contentDAO.findAnnouncement(id);
        if (existing == null) {
            return;
        }
        DaoSupport.transaction("The announcement could not be removed.", connection -> {
            contentDAO.deleteAnnouncement(connection, id);
            activityService.record(connection, adminId, "CONTENT_UPDATED",
                    "Announcement removed: " + existing.getTitle(), "ANNOUNCEMENT", id);
        });
    }

    public void saveStation(long adminId, Station station) throws ValidationException {
        Station existing = trainDAO.findStation(station.getId());
        Validators validators = new Validators();
        if (existing == null) {
            validators.add("stationId", "Choose a station that already exists.");
        }
        validators.required("stationName", station.getStationName(), "Station name");
        validators.maxLength("stationName", station.getStationName(), 120, "Station name");
        validators.required("city", station.getCity(), "City");
        validators.maxLength("city", station.getCity(), 80, "City");
        validators.required("stateName", station.getStateName(), "State");
        validators.maxLength("stateName", station.getStateName(), 80, "State");
        validators.finish();
        DaoSupport.transaction("The station could not be saved.", connection -> {
            trainDAO.updateStation(connection, station);
            activityService.record(connection, adminId, "CONTENT_UPDATED",
                    "Station updated: " + existing.getStationCode(), "STATION", station.getId());
        });
    }

    private String storeUpload(Part upload, Validators validators) throws IOException {
        if (upload.getSize() > 2_097_152) {
            validators.add("bannerFile", "The image must be 2 MB or smaller.");
            return null;
        }
        String type = upload.getContentType() == null ? "" : upload.getContentType().toLowerCase();
        if (!type.equals("image/jpeg") && !type.equals("image/png") && !type.equals("image/webp")) {
            validators.add("bannerFile", "Upload a JPG, PNG, or WebP image.");
            return null;
        }
        try (InputStream input = upload.getInputStream()) {
            return UploadStore.save(input, type);
        }
    }

    private int activityLimit(String raw) {
        try {
            int limit = Integer.parseInt(raw == null ? "5" : raw);
            if (limit == 3 || limit == 5 || limit == 8) {
                return limit;
            }
        } catch (NumberFormatException ex) {
            return 5;
        }
        return 5;
    }

    private String orDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
