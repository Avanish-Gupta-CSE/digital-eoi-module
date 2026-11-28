package com.uco.eoi.report;

import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * The child forms of the Annex II entry screen for Parts I, III, IV and V: one add and one
 * delete for the directors, allied activities, other banks, facilities, derivative contracts
 * and currency positions. Part VI lives in {@link ReportRubricController}.
 *
 * Thin controller: read the input, call the repository, send the officer back to the entry
 * screen. Every endpoint ends at the same place, so the officer never loses the report he was
 * working on.
 */
@Controller
public class ReportChildController {

    private final ReportChildRepository childRepository;
    private final ReportExposureRepository exposureRepository;

    public ReportChildController(ReportChildRepository childRepository,
            ReportExposureRepository exposureRepository) {
        this.childRepository = childRepository;
        this.exposureRepository = exposureRepository;
    }

    /** Part I.III: adds a director or a partner of the borrower. */
    @PostMapping("/reports/{id}/directors/add")
    public String addDirector(@PathVariable("id") long id,
            @RequestParam Map<String, String> p) {
        childRepository.insertDirector(id, p.get("name"), p.get("designation"), p.get("netWorth"));
        return backToEdit(id);
    }

    /** Part I.III: removes a director or a partner. */
    @PostMapping("/reports/{id}/directors/{rowId}/delete")
    public String deleteDirector(@PathVariable("id") long id,
            @PathVariable("rowId") long rowId) {
        childRepository.deleteDirector(rowId);
        return backToEdit(id);
    }

    /** Part I.IV(b): adds an allied business activity of the same group. */
    @PostMapping("/reports/{id}/allied/add")
    public String addAllied(@PathVariable("id") long id,
            @RequestParam Map<String, String> p) {
        childRepository.insertAllied(id, p.get("activityName"));
        return backToEdit(id);
    }

    /** Part I.IV(b): removes an allied business activity. */
    @PostMapping("/reports/{id}/allied/{rowId}/delete")
    public String deleteAllied(@PathVariable("id") long id,
            @PathVariable("rowId") long rowId) {
        childRepository.deleteAllied(rowId);
        return backToEdit(id);
    }

    /** Part I.V: adds another bank financing the same borrower. */
    @PostMapping("/reports/{id}/banks/add")
    public String addBank(@PathVariable("id") long id,
            @RequestParam Map<String, String> p) {
        childRepository.insertBank(id, p.get("bankName"));
        return backToEdit(id);
    }

    /** Part I.V: removes another financing bank. */
    @PostMapping("/reports/{id}/banks/{rowId}/delete")
    public String deleteBank(@PathVariable("id") long id,
            @PathVariable("rowId") long rowId) {
        childRepository.deleteBank(rowId);
        return backToEdit(id);
    }

    /** Part III: adds a credit facility, with its security when one was typed. */
    @PostMapping("/reports/{id}/facilities/add")
    public String addFacility(@PathVariable("id") long id,
            @RequestParam Map<String, String> p) {
        exposureRepository.insertFacility(id, p.get("facilityType"), p.get("purpose"),
                p.get("facilityDate"), p.get("sanctionedAmount"), p.get("balanceOutstanding"),
                p.get("repaymentTerms"), p.get("defaultsCommitments"), p.get("securityText"));
        return backToEdit(id);
    }

    /** Part III: removes a facility and the security held against it. */
    @PostMapping("/reports/{id}/facilities/{rowId}/delete")
    public String deleteFacility(@PathVariable("id") long id,
            @PathVariable("rowId") long rowId) {
        exposureRepository.deleteFacility(rowId);
        return backToEdit(id);
    }

    /** Part IV: adds a derivative contract. */
    @PostMapping("/reports/{id}/derivatives/add")
    public String addDerivative(@PathVariable("id") long id,
            @RequestParam Map<String, String> p) {
        exposureRepository.insertDerivative(id, number(p.get("serialNo")), p.get("contractNature"),
                p.get("notionalAmount"), p.get("weightedAvgMaturity"), p.get("positiveMtm"),
                p.get("npaAmount"), p.get("restructuredNotionalAmount"), p.get("reasons"));
        return backToEdit(id);
    }

    /** Part IV: removes a derivative contract. */
    @PostMapping("/reports/{id}/derivatives/{rowId}/delete")
    public String deleteDerivative(@PathVariable("id") long id,
            @PathVariable("rowId") long rowId) {
        exposureRepository.deleteDerivative(rowId);
        return backToEdit(id);
    }

    /** Part V: adds a currency position. */
    @PostMapping("/reports/{id}/currencies/add")
    public String addCurrency(@PathVariable("id") long id,
            @RequestParam Map<String, String> p) {
        exposureRepository.insertCurrency(id, p.get("currencyCode"), p.get("shortLong"),
                p.get("shortShort"), p.get("longLong"), p.get("longShort"));
        return backToEdit(id);
    }

    /** Part V: removes a currency position. */
    @PostMapping("/reports/{id}/currencies/{rowId}/delete")
    public String deleteCurrency(@PathVariable("id") long id,
            @PathVariable("rowId") long rowId) {
        exposureRepository.deleteCurrency(rowId);
        return backToEdit(id);
    }

    /** A typed whole number; an empty box counts as zero. */
    private static int number(String value) {
        if (value == null || value.isBlank()) {
            return 0;
        }
        return (int) Double.parseDouble(value.trim());
    }

    /** Every child form ends at the entry screen, with the "saved" flag showing. */
    private static String backToEdit(long id) {
        return "redirect:/reports/" + id + "/edit?saved=1";
    }
}
