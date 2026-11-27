package com.uco.eoi.report;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * The mock Finacle reader, used by the h2 profile and by the tests.
 *
 * It fills the one field Finacle is sure about - the IRAC classification - and only when
 * the maker has left it empty. The live reader will do the same job from the Finacle
 * views, so nothing else in the module has to change.
 */
@Service
public class MockFinacleDataSource implements FinacleDataSource {

    private static final String FILL_IRAC_CLASSIFICATION =
            "UPDATE eoi_report SET irac_classification = 'Standard' "
                    + "WHERE id = ? AND irac_classification IS NULL";

    private final JdbcTemplate jdbc;

    public MockFinacleDataSource(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public int syncReport(long reportId) {
        return jdbc.update(FILL_IRAC_CLASSIFICATION, reportId);
    }
}
