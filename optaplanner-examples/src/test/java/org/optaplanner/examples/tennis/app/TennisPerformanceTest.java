/*
 * Copyright 2013 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.examples.tennis.app;

import java.io.File;

import org.junit.Test;
import org.optaplanner.core.config.solver.EnvironmentMode;
import org.optaplanner.examples.cloudbalancing.persistence.CloudBalancingDao;
import org.optaplanner.examples.common.app.SolverPerformanceTest;
import org.optaplanner.examples.common.persistence.SolutionDao;
import org.optaplanner.examples.tennis.persistence.TennisDao;

public class TennisPerformanceTest extends SolverPerformanceTest {

    @Override
    protected String createSolverConfigResource() {
        return "/org/optaplanner/examples/tennis/solver/tennisSolverConfig.xml";
    }

    @Override
    protected SolutionDao createSolutionDao() {
        return new TennisDao();
    }

    // ************************************************************************
    // Tests
    // ************************************************************************

    @Test(timeout = 600000)
    public void solveModel_munich_7teams() {
        File unsolvedDataFile = new File("data/tennis/unsolved/munich-7teams.xml");
        runSpeedTest(unsolvedDataFile, "0hard/-742medium/-288soft");
    }

    @Test(timeout = 600000)
    public void solveModel_munich_7teamsFastAssert() {
        File unsolvedDataFile = new File("data/tennis/unsolved/munich-7teams.xml");
        runSpeedTest(unsolvedDataFile, "0hard/-742medium/-294soft", EnvironmentMode.FAST_ASSERT);
    }

}
