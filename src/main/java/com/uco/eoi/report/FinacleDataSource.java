package com.uco.eoi.report;

/**
 * The one place where Finacle data enters the module.
 *
 * This is a real seam: the h2 profile runs the mock, the UAT profile will run the live
 * reader, and the workflow service does not know which one it is talking to.
 */
public interface FinacleDataSource {

    /**
     * Pulls the Finacle figures of one report into the module.
     *
     * @param reportId the report to fill from Finacle
     * @return how many rows of the report were changed
     */
    int syncReport(long reportId);
}
