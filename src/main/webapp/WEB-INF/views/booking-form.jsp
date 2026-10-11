<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="fragments/layout-start.jsp" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<div class="page-head"><h1>Record booking</h1><a href="${ctx}/bookings">Back to bookings</a></div>
<section class="card">
    <c:if test="${not empty errors.form}"><p class="form-error"><c:out value="${errors.form}"/></p></c:if>
    <form method="post" action="${ctx}/bookings/save">
        <input type="hidden" name="csrfToken" value="${csrfToken}">
        <div class="form-grid">
            <div class="field"><label for="userId">Passenger</label>
                <select id="userId" name="userId" required>
                    <option value="">Choose a passenger</option>
                    <c:forEach var="user" items="${users}"><option value="${user.id}" ${form.userId == user.id ? 'selected' : ''}><c:out value="${user.fullName}"/></option></c:forEach>
                </select><span class="error"><c:out value="${errors.userId}"/></span></div>
            <div class="field"><label for="trainId">Train</label>
                <select id="trainId" name="trainId" data-train-select required>
                    <option value="">Choose a train</option>
                    <c:forEach var="train" items="${trains}">
                        <c:if test="${train.status == 'ACTIVE'}">
                            <option value="${train.id}" data-source="${train.sourceCode}" data-destination="${train.destinationCode}" ${form.trainId == train.id ? 'selected' : ''}>
                                <c:out value="${train.trainNumber}"/> · <c:out value="${train.trainName}"/>
                            </option>
                        </c:if>
                    </c:forEach>
                </select><span class="error"><c:out value="${errors.trainId}"/></span></div>
            <div class="field"><label for="source">From</label>
                <select id="source" name="source" data-source-select required>
                    <c:forEach var="station" items="${stations}"><option value="${station.stationCode}" ${form.sourceStation == station.stationCode ? 'selected' : ''}><c:out value="${station.label}"/></option></c:forEach>
                </select><span class="error"><c:out value="${errors.source}"/></span></div>
            <div class="field"><label for="destination">To</label>
                <select id="destination" name="destination" data-destination-select required>
                    <c:forEach var="station" items="${stations}"><option value="${station.stationCode}" ${form.destinationStation == station.stationCode ? 'selected' : ''}><c:out value="${station.label}"/></option></c:forEach>
                </select><span class="error"><c:out value="${errors.destination}"/></span></div>
            <div class="field"><label for="travelClass">Class</label>
                <select id="travelClass" name="travelClass" required>
                    <c:forEach var="item" items="${['SL','3A','2A','1A','CC']}"><option value="${item}" ${form.travelClass == item ? 'selected' : ''}><c:out value="${item}"/></option></c:forEach>
                </select><span class="error"><c:out value="${errors.travelClass}"/></span></div>
            <div class="field"><label for="serviceType">Service</label>
                <select id="serviceType" name="serviceType" required>
                    <c:forEach var="service" items="${services}"><c:if test="${service.offered}"><option value="${service.code}" ${form.serviceType == service.code ? 'selected' : ''}><c:out value="${service.name}"/></option></c:if></c:forEach>
                </select><span class="error"><c:out value="${errors.serviceType}"/></span></div>
            <div class="field"><label for="travelDate">Travel date</label><input id="travelDate" name="travelDate" type="date" required value="${form.travelDate}"><span class="error"><c:out value="${errors.travelDate}"/></span></div>
            <div class="field"><label for="amount">Amount (INR)</label><input id="amount" name="amount" inputmode="decimal" required value="${form.totalAmount}"><span class="error"><c:out value="${errors.amount}"/></span></div>
            <div class="field"><label for="status">Status</label>
                <select id="status" name="status">
                    <c:forEach var="item" items="${['CONFIRMED','WAITING','RAC','CANCELLED']}"><option value="${item}" ${form.bookingStatus == item ? 'selected' : ''}><c:out value="${item}"/></option></c:forEach>
                </select><span class="error"><c:out value="${errors.status}"/></span></div>
        </div>
        <button class="primary" type="submit">Save booking</button>
    </form>
</section>
<%@ include file="fragments/layout-end.jsp" %>
