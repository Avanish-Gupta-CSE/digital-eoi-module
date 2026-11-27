package com.uco.eoi.report;

import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * The Annex II fields the maker edits on the entry screen: Part I (the borrower's own
 * details) and Part II (the credit quality indicators).
 *
 * A plain holder with getters and setters, because the page binds every field by its name.
 * There are no validation annotations on it: the required-field rule is checked on the
 * server, in ReportEditController#validate, so the bank can change a rule without adding a
 * validation library to the project.
 */
public class ReportEditForm {

    private String address;
    private String constitution;
    private String mainActivity;
    private String groupAffiliation;
    private String associateConcerns;

    private String iracClassification;
    private String internalRating;
    private String internalRatingNarration;
    private String externalRating;
    private String annualReportLatest;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate annualReportAsOn;

    private String changesShareholding;
    private String specialInformation;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getConstitution() {
        return constitution;
    }

    public void setConstitution(String constitution) {
        this.constitution = constitution;
    }

    public String getMainActivity() {
        return mainActivity;
    }

    public void setMainActivity(String mainActivity) {
        this.mainActivity = mainActivity;
    }

    public String getGroupAffiliation() {
        return groupAffiliation;
    }

    public void setGroupAffiliation(String groupAffiliation) {
        this.groupAffiliation = groupAffiliation;
    }

    public String getAssociateConcerns() {
        return associateConcerns;
    }

    public void setAssociateConcerns(String associateConcerns) {
        this.associateConcerns = associateConcerns;
    }

    public String getIracClassification() {
        return iracClassification;
    }

    public void setIracClassification(String iracClassification) {
        this.iracClassification = iracClassification;
    }

    public String getInternalRating() {
        return internalRating;
    }

    public void setInternalRating(String internalRating) {
        this.internalRating = internalRating;
    }

    public String getInternalRatingNarration() {
        return internalRatingNarration;
    }

    public void setInternalRatingNarration(String internalRatingNarration) {
        this.internalRatingNarration = internalRatingNarration;
    }

    public String getExternalRating() {
        return externalRating;
    }

    public void setExternalRating(String externalRating) {
        this.externalRating = externalRating;
    }

    public String getAnnualReportLatest() {
        return annualReportLatest;
    }

    public void setAnnualReportLatest(String annualReportLatest) {
        this.annualReportLatest = annualReportLatest;
    }

    public LocalDate getAnnualReportAsOn() {
        return annualReportAsOn;
    }

    public void setAnnualReportAsOn(LocalDate annualReportAsOn) {
        this.annualReportAsOn = annualReportAsOn;
    }

    public String getChangesShareholding() {
        return changesShareholding;
    }

    public void setChangesShareholding(String changesShareholding) {
        this.changesShareholding = changesShareholding;
    }

    public String getSpecialInformation() {
        return specialInformation;
    }

    public void setSpecialInformation(String specialInformation) {
        this.specialInformation = specialInformation;
    }
}
