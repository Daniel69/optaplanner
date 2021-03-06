/*
 * Copyright 2012 JBoss Inc
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

package org.optaplanner.core.impl.heuristic.selector.value.chained;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import org.junit.Test;
import org.optaplanner.core.impl.domain.variable.descriptor.GenuineVariableDescriptor;
import org.optaplanner.core.impl.heuristic.selector.SelectorTestUtils;
import org.optaplanner.core.impl.heuristic.selector.value.EntityIndependentValueSelector;
import org.optaplanner.core.impl.phase.scope.AbstractSolverPhaseScope;
import org.optaplanner.core.impl.phase.scope.AbstractStepScope;
import org.optaplanner.core.impl.score.director.ScoreDirector;
import org.optaplanner.core.impl.solver.scope.DefaultSolverScope;
import org.optaplanner.core.impl.testdata.domain.chained.TestdataChainedAnchor;
import org.optaplanner.core.impl.testdata.domain.chained.TestdataChainedEntity;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import static org.optaplanner.core.impl.testdata.util.PlannerAssert.*;

public class DefaultSubChainSelectorTest {

    @Test
    public void original() {
        GenuineVariableDescriptor variableDescriptor = SelectorTestUtils.mockVariableDescriptor(
                TestdataChainedEntity.class, "chainedObject");
        when(variableDescriptor.isChained()).thenReturn(true);
        ScoreDirector scoreDirector = mock(ScoreDirector.class);

        TestdataChainedAnchor a0 = new TestdataChainedAnchor("a0");
        TestdataChainedEntity a1 = new TestdataChainedEntity("a1", a0);
        TestdataChainedEntity a2 = new TestdataChainedEntity("a2", a1);
        TestdataChainedEntity a3 = new TestdataChainedEntity("a3", a2);
        TestdataChainedEntity a4 = new TestdataChainedEntity("a4", a3);

        TestdataChainedAnchor b0 = new TestdataChainedAnchor("b0");
        TestdataChainedEntity b1 = new TestdataChainedEntity("b1", b0);
        TestdataChainedEntity b2 = new TestdataChainedEntity("b2", b1);

        SelectorTestUtils.mockMethodGetTrailingEntity(scoreDirector, variableDescriptor,
                new TestdataChainedEntity[]{a1, a2, a3, a4, b1, b2});

        EntityIndependentValueSelector valueSelector = SelectorTestUtils.mockEntityIndependentValueSelector(
                variableDescriptor,
                a0, a1, a2, a3, a4, b0, b1, b2);

        DefaultSubChainSelector subChainSelector = new DefaultSubChainSelector(
                valueSelector, false, 1, Integer.MAX_VALUE);

        DefaultSolverScope solverScope = mock(DefaultSolverScope.class);
        when(solverScope.getScoreDirector()).thenReturn(scoreDirector);
        subChainSelector.solvingStarted(solverScope);

        AbstractSolverPhaseScope phaseScopeA = mock(AbstractSolverPhaseScope.class);
        when(phaseScopeA.getSolverScope()).thenReturn(solverScope);
        subChainSelector.phaseStarted(phaseScopeA);

        AbstractStepScope stepScopeA1 = mock(AbstractStepScope.class);
        when(stepScopeA1.getPhaseScope()).thenReturn(phaseScopeA);
        subChainSelector.stepStarted(stepScopeA1);
        assertAllCodesOfSubChainSelector(subChainSelector,
                "[a1]", "[a1, a2]", "[a1, a2, a3]", "[a1, a2, a3, a4]",
                "[a2]", "[a2, a3]", "[a2, a3, a4]",
                "[a3]", "[a3, a4]",
                "[a4]",
                "[b1]", "[b1, b2]",
                "[b2]");
        subChainSelector.stepEnded(stepScopeA1);

        a4.setChainedObject(a2);
        a3.setChainedObject(b1);
        b2.setChainedObject(a3);

        AbstractStepScope stepScopeA2 = mock(AbstractStepScope.class);
        when(stepScopeA2.getPhaseScope()).thenReturn(phaseScopeA);
        subChainSelector.stepStarted(stepScopeA2);
        assertAllCodesOfSubChainSelector(subChainSelector,
                "[a1]", "[a1, a2]", "[a1, a2, a4]",
                "[a2]", "[a2, a4]",
                "[a4]",
                "[b1]", "[b1, a3]", "[b1, a3, b2]",
                "[a3]", "[a3, b2]",
                "[b2]");
        subChainSelector.stepEnded(stepScopeA2);

        subChainSelector.phaseEnded(phaseScopeA);

        AbstractSolverPhaseScope phaseScopeB = mock(AbstractSolverPhaseScope.class);
        when(phaseScopeB.getSolverScope()).thenReturn(solverScope);
        subChainSelector.phaseStarted(phaseScopeB);

        AbstractStepScope stepScopeB1 = mock(AbstractStepScope.class);
        when(stepScopeB1.getPhaseScope()).thenReturn(phaseScopeB);
        subChainSelector.stepStarted(stepScopeB1);
        assertAllCodesOfSubChainSelector(subChainSelector,
                "[a1]", "[a1, a2]", "[a1, a2, a4]",
                "[a2]", "[a2, a4]",
                "[a4]",
                "[b1]", "[b1, a3]", "[b1, a3, b2]",
                "[a3]", "[a3, b2]",
                "[b2]");
        subChainSelector.stepEnded(stepScopeB1);

        subChainSelector.phaseEnded(phaseScopeB);

        subChainSelector.solvingEnded(solverScope);

        verifySolverPhaseLifecycle(valueSelector, 1, 2, 3);
    }

    @Test
    public void emptyEntitySelectorOriginal() {
        GenuineVariableDescriptor variableDescriptor = SelectorTestUtils.mockVariableDescriptor(
                TestdataChainedEntity.class, "chainedObject");
        when(variableDescriptor.isChained()).thenReturn(true);
        ScoreDirector scoreDirector = mock(ScoreDirector.class);

        TestdataChainedAnchor a0 = new TestdataChainedAnchor("a0");
        TestdataChainedAnchor b0 = new TestdataChainedAnchor("b0");

        SelectorTestUtils.mockMethodGetTrailingEntity(scoreDirector, variableDescriptor,
                new TestdataChainedEntity[]{});

        EntityIndependentValueSelector valueSelector = SelectorTestUtils.mockEntityIndependentValueSelector(
                variableDescriptor,
                a0, b0);

        DefaultSubChainSelector subChainSelector = new DefaultSubChainSelector(
                valueSelector, false, 1, Integer.MAX_VALUE);

        DefaultSolverScope solverScope = mock(DefaultSolverScope.class);
        when(solverScope.getScoreDirector()).thenReturn(scoreDirector);
        subChainSelector.solvingStarted(solverScope);

        AbstractSolverPhaseScope phaseScopeA = mock(AbstractSolverPhaseScope.class);
        when(phaseScopeA.getSolverScope()).thenReturn(solverScope);
        subChainSelector.phaseStarted(phaseScopeA);

        AbstractStepScope stepScopeA1 = mock(AbstractStepScope.class);
        when(stepScopeA1.getPhaseScope()).thenReturn(phaseScopeA);
        subChainSelector.stepStarted(stepScopeA1);
        assertAllCodesOfSubChainSelector(subChainSelector);
        subChainSelector.stepEnded(stepScopeA1);

        AbstractStepScope stepScopeA2 = mock(AbstractStepScope.class);
        when(stepScopeA2.getPhaseScope()).thenReturn(phaseScopeA);
        subChainSelector.stepStarted(stepScopeA2);
        assertAllCodesOfSubChainSelector(subChainSelector);
        subChainSelector.stepEnded(stepScopeA2);

        subChainSelector.phaseEnded(phaseScopeA);

        AbstractSolverPhaseScope phaseScopeB = mock(AbstractSolverPhaseScope.class);
        when(phaseScopeB.getSolverScope()).thenReturn(solverScope);
        subChainSelector.phaseStarted(phaseScopeB);

        AbstractStepScope stepScopeB1 = mock(AbstractStepScope.class);
        when(stepScopeB1.getPhaseScope()).thenReturn(phaseScopeB);
        subChainSelector.stepStarted(stepScopeB1);
        assertAllCodesOfSubChainSelector(subChainSelector);
        subChainSelector.stepEnded(stepScopeB1);

        subChainSelector.phaseEnded(phaseScopeB);

        subChainSelector.solvingEnded(solverScope);

        verifySolverPhaseLifecycle(valueSelector, 1, 2, 3);
    }

    private void assertAllCodesOfSubChainSelector(SubChainSelector subChainSelector, String... codes) {
        assertAllCodesOfIterator(subChainSelector.iterator(), codes);
        assertEquals(true, subChainSelector.isCountable());
        assertEquals(false, subChainSelector.isNeverEnding());
        assertEquals(codes.length, subChainSelector.getSize());
    }

    @Test
    public void originalMinimum2Maximum3() {
        GenuineVariableDescriptor variableDescriptor = SelectorTestUtils.mockVariableDescriptor(
                TestdataChainedEntity.class, "chainedObject");
        when(variableDescriptor.isChained()).thenReturn(true);
        ScoreDirector scoreDirector = mock(ScoreDirector.class);

        TestdataChainedAnchor a0 = new TestdataChainedAnchor("a0");
        TestdataChainedEntity a1 = new TestdataChainedEntity("a1", a0);
        TestdataChainedEntity a2 = new TestdataChainedEntity("a2", a1);
        TestdataChainedEntity a3 = new TestdataChainedEntity("a3", a2);
        TestdataChainedEntity a4 = new TestdataChainedEntity("a4", a3);

        TestdataChainedAnchor b0 = new TestdataChainedAnchor("b0");
        TestdataChainedEntity b1 = new TestdataChainedEntity("b1", b0);
        TestdataChainedEntity b2 = new TestdataChainedEntity("b2", b1);

        SelectorTestUtils.mockMethodGetTrailingEntity(scoreDirector, variableDescriptor,
                new TestdataChainedEntity[]{a1, a2, a3, a4, b1, b2});

        EntityIndependentValueSelector valueSelector = SelectorTestUtils.mockEntityIndependentValueSelector(
                variableDescriptor,
                a0, a1, a2, a3, a4, b0, b1, b2);

        DefaultSubChainSelector subChainSelector = new DefaultSubChainSelector(
                valueSelector, false, 2, 3);

        DefaultSolverScope solverScope = mock(DefaultSolverScope.class);
        when(solverScope.getScoreDirector()).thenReturn(scoreDirector);
        subChainSelector.solvingStarted(solverScope);

        AbstractSolverPhaseScope phaseScopeA = mock(AbstractSolverPhaseScope.class);
        when(phaseScopeA.getSolverScope()).thenReturn(solverScope);
        subChainSelector.phaseStarted(phaseScopeA);

        AbstractStepScope stepScopeA1 = mock(AbstractStepScope.class);
        when(stepScopeA1.getPhaseScope()).thenReturn(phaseScopeA);
        subChainSelector.stepStarted(stepScopeA1);

        assertAllCodesOfSubChainSelector(subChainSelector,
                "[a1, a2]", "[a1, a2, a3]", "[a2, a3]", "[a2, a3, a4]", "[a3, a4]", "[b1, b2]");

        subChainSelector.stepEnded(stepScopeA1);

        subChainSelector.phaseEnded(phaseScopeA);

        subChainSelector.solvingEnded(solverScope);

        verifySolverPhaseLifecycle(valueSelector, 1, 1, 1);
    }

    @Test
    public void originalMinimum3Maximum3() {
        GenuineVariableDescriptor variableDescriptor = SelectorTestUtils.mockVariableDescriptor(
                TestdataChainedEntity.class, "chainedObject");
        when(variableDescriptor.isChained()).thenReturn(true);
        ScoreDirector scoreDirector = mock(ScoreDirector.class);

        TestdataChainedAnchor a0 = new TestdataChainedAnchor("a0");
        TestdataChainedEntity a1 = new TestdataChainedEntity("a1", a0);
        TestdataChainedEntity a2 = new TestdataChainedEntity("a2", a1);
        TestdataChainedEntity a3 = new TestdataChainedEntity("a3", a2);
        TestdataChainedEntity a4 = new TestdataChainedEntity("a4", a3);

        TestdataChainedAnchor b0 = new TestdataChainedAnchor("b0");
        TestdataChainedEntity b1 = new TestdataChainedEntity("b1", b0);
        TestdataChainedEntity b2 = new TestdataChainedEntity("b2", b1);

        SelectorTestUtils.mockMethodGetTrailingEntity(scoreDirector, variableDescriptor,
                new TestdataChainedEntity[]{a1, a2, a3, a4, b1, b2});

        EntityIndependentValueSelector valueSelector = SelectorTestUtils.mockEntityIndependentValueSelector(
                variableDescriptor,
                a0, a1, a2, a3, a4, b0, b1, b2);

        DefaultSubChainSelector subChainSelector = new DefaultSubChainSelector(
                valueSelector, false, 3, 3);

        DefaultSolverScope solverScope = mock(DefaultSolverScope.class);
        when(solverScope.getScoreDirector()).thenReturn(scoreDirector);
        subChainSelector.solvingStarted(solverScope);

        AbstractSolverPhaseScope phaseScopeA = mock(AbstractSolverPhaseScope.class);
        when(phaseScopeA.getSolverScope()).thenReturn(solverScope);
        subChainSelector.phaseStarted(phaseScopeA);

        AbstractStepScope stepScopeA1 = mock(AbstractStepScope.class);
        when(stepScopeA1.getPhaseScope()).thenReturn(phaseScopeA);
        subChainSelector.stepStarted(stepScopeA1);

        assertAllCodesOfSubChainSelector(subChainSelector, "[a1, a2, a3]", "[a2, a3, a4]");

        subChainSelector.stepEnded(stepScopeA1);

        subChainSelector.phaseEnded(phaseScopeA);

        subChainSelector.solvingEnded(solverScope);

        verifySolverPhaseLifecycle(valueSelector, 1, 1, 1);
    }

    @Test
    public void random() {
        GenuineVariableDescriptor variableDescriptor = SelectorTestUtils.mockVariableDescriptor(
                TestdataChainedEntity.class, "chainedObject");
        when(variableDescriptor.isChained()).thenReturn(true);
        ScoreDirector scoreDirector = mock(ScoreDirector.class);

        TestdataChainedAnchor a0 = new TestdataChainedAnchor("a0");
        TestdataChainedEntity a1 = new TestdataChainedEntity("a1", a0);
        TestdataChainedEntity a2 = new TestdataChainedEntity("a2", a1);
        TestdataChainedEntity a3 = new TestdataChainedEntity("a3", a2);
        TestdataChainedEntity a4 = new TestdataChainedEntity("a4", a3);

        SelectorTestUtils.mockMethodGetTrailingEntity(scoreDirector, variableDescriptor,
                new TestdataChainedEntity[]{a1, a2, a3, a4});

        EntityIndependentValueSelector valueSelector = SelectorTestUtils.mockEntityIndependentValueSelector(
                variableDescriptor,
                a0, a1, a2, a3, a4);

        DefaultSubChainSelector subChainSelector = new DefaultSubChainSelector(
                valueSelector, true, 1, Integer.MAX_VALUE);

        DefaultSolverScope solverScope = mock(DefaultSolverScope.class);
        when(solverScope.getScoreDirector()).thenReturn(scoreDirector);
        when(solverScope.getWorkingRandom()).thenReturn(new Random(0L));
        subChainSelector.solvingStarted(solverScope);

        AbstractSolverPhaseScope phaseScopeA = mock(AbstractSolverPhaseScope.class);
        when(phaseScopeA.getSolverScope()).thenReturn(solverScope);
        subChainSelector.phaseStarted(phaseScopeA);

        AbstractStepScope stepScopeA1 = mock(AbstractStepScope.class);
        when(stepScopeA1.getPhaseScope()).thenReturn(phaseScopeA);
        subChainSelector.stepStarted(stepScopeA1);

        assertContainsCodesOfNeverEndingSubChainSelector(subChainSelector,
                new SubChain(Arrays.<Object>asList(a1)),
                new SubChain(Arrays.<Object>asList(a2)),
                new SubChain(Arrays.<Object>asList(a3)),
                new SubChain(Arrays.<Object>asList(a4)),
                new SubChain(Arrays.<Object>asList(a1, a2)),
                new SubChain(Arrays.<Object>asList(a2, a3)),
                new SubChain(Arrays.<Object>asList(a3, a4)),
                new SubChain(Arrays.<Object>asList(a1, a2, a3)),
                new SubChain(Arrays.<Object>asList(a2, a3, a4)),
                new SubChain(Arrays.<Object>asList(a1, a2, a3, a4)));

        subChainSelector.stepEnded(stepScopeA1);

        subChainSelector.phaseEnded(phaseScopeA);

        subChainSelector.solvingEnded(solverScope);

        verifySolverPhaseLifecycle(valueSelector, 1, 1, 1);
    }

    @Test
    public void randomMinimum2Maximum3() {
        GenuineVariableDescriptor variableDescriptor = SelectorTestUtils.mockVariableDescriptor(
                TestdataChainedEntity.class, "chainedObject");
        when(variableDescriptor.isChained()).thenReturn(true);
        ScoreDirector scoreDirector = mock(ScoreDirector.class);

        TestdataChainedAnchor a0 = new TestdataChainedAnchor("a0");
        TestdataChainedEntity a1 = new TestdataChainedEntity("a1", a0);
        TestdataChainedEntity a2 = new TestdataChainedEntity("a2", a1);
        TestdataChainedEntity a3 = new TestdataChainedEntity("a3", a2);
        TestdataChainedEntity a4 = new TestdataChainedEntity("a4", a3);

        SelectorTestUtils.mockMethodGetTrailingEntity(scoreDirector, variableDescriptor,
                new TestdataChainedEntity[]{a1, a2, a3, a4});

        EntityIndependentValueSelector valueSelector = SelectorTestUtils.mockEntityIndependentValueSelector(
                variableDescriptor,
                a0, a1, a2, a3, a4);

        DefaultSubChainSelector subChainSelector = new DefaultSubChainSelector(
                valueSelector, true, 2, 3);

        DefaultSolverScope solverScope = mock(DefaultSolverScope.class);
        when(solverScope.getScoreDirector()).thenReturn(scoreDirector);
        when(solverScope.getWorkingRandom()).thenReturn(new Random(0L));
        subChainSelector.solvingStarted(solverScope);

        AbstractSolverPhaseScope phaseScopeA = mock(AbstractSolverPhaseScope.class);
        when(phaseScopeA.getSolverScope()).thenReturn(solverScope);
        subChainSelector.phaseStarted(phaseScopeA);

        AbstractStepScope stepScopeA1 = mock(AbstractStepScope.class);
        when(stepScopeA1.getPhaseScope()).thenReturn(phaseScopeA);
        subChainSelector.stepStarted(stepScopeA1);

        assertContainsCodesOfNeverEndingSubChainSelector(subChainSelector,
                new SubChain(Arrays.<Object>asList(a1, a2)),
                new SubChain(Arrays.<Object>asList(a2, a3)),
                new SubChain(Arrays.<Object>asList(a3, a4)),
                new SubChain(Arrays.<Object>asList(a1, a2, a3)),
                new SubChain(Arrays.<Object>asList(a2, a3, a4)));

        subChainSelector.stepEnded(stepScopeA1);

        subChainSelector.phaseEnded(phaseScopeA);

        subChainSelector.solvingEnded(solverScope);

        verifySolverPhaseLifecycle(valueSelector, 1, 1, 1);
    }

    @Test
    public void randomMinimum3Maximum3() {
        GenuineVariableDescriptor variableDescriptor = SelectorTestUtils.mockVariableDescriptor(
                TestdataChainedEntity.class, "chainedObject");
        when(variableDescriptor.isChained()).thenReturn(true);
        ScoreDirector scoreDirector = mock(ScoreDirector.class);

        TestdataChainedAnchor a0 = new TestdataChainedAnchor("a0");
        TestdataChainedEntity a1 = new TestdataChainedEntity("a1", a0);
        TestdataChainedEntity a2 = new TestdataChainedEntity("a2", a1);
        TestdataChainedEntity a3 = new TestdataChainedEntity("a3", a2);
        TestdataChainedEntity a4 = new TestdataChainedEntity("a4", a3);

        SelectorTestUtils.mockMethodGetTrailingEntity(scoreDirector, variableDescriptor,
                new TestdataChainedEntity[]{a1, a2, a3, a4});

        EntityIndependentValueSelector valueSelector = SelectorTestUtils.mockEntityIndependentValueSelector(
                variableDescriptor,
                a0, a1, a2, a3, a4);

        DefaultSubChainSelector subChainSelector = new DefaultSubChainSelector(
                valueSelector, true, 3, 3);

        DefaultSolverScope solverScope = mock(DefaultSolverScope.class);
        when(solverScope.getScoreDirector()).thenReturn(scoreDirector);
        when(solverScope.getWorkingRandom()).thenReturn(new Random(0L));
        subChainSelector.solvingStarted(solverScope);

        AbstractSolverPhaseScope phaseScopeA = mock(AbstractSolverPhaseScope.class);
        when(phaseScopeA.getSolverScope()).thenReturn(solverScope);
        subChainSelector.phaseStarted(phaseScopeA);

        AbstractStepScope stepScopeA1 = mock(AbstractStepScope.class);
        when(stepScopeA1.getPhaseScope()).thenReturn(phaseScopeA);
        subChainSelector.stepStarted(stepScopeA1);

        assertContainsCodesOfNeverEndingSubChainSelector(subChainSelector,
                new SubChain(Arrays.<Object>asList(a1, a2, a3)),
                new SubChain(Arrays.<Object>asList(a2, a3, a4)));

        subChainSelector.stepEnded(stepScopeA1);

        subChainSelector.phaseEnded(phaseScopeA);

        subChainSelector.solvingEnded(solverScope);

        verifySolverPhaseLifecycle(valueSelector, 1, 1, 1);
    }

    private void assertContainsCodesOfNeverEndingSubChainSelector(
            DefaultSubChainSelector subChainSelector, SubChain... subChains) {
        Iterator<SubChain> iterator = subChainSelector.iterator();
        assertNotNull(iterator);
        int selectionSize = subChains.length;
        Map<SubChain, Integer> subChainCountMap = new HashMap<SubChain, Integer>(selectionSize);
        for (int i = 0; i < selectionSize * 10; i++) {
            collectNextSubChain(iterator, subChainCountMap);
        }
        for (SubChain subChain : subChains) {
            Integer count = subChainCountMap.remove(subChain);
            assertNotNull("The subChain (" + subChain + ") was not collected.", count);
        }
        assertTrue(subChainCountMap.isEmpty());
        assertTrue(iterator.hasNext());
        assertEquals(true, subChainSelector.isCountable());
        assertEquals(true, subChainSelector.isNeverEnding());
        assertEquals((long) selectionSize, subChainSelector.getSize());
    }

    private void collectNextSubChain(Iterator<SubChain> iterator, Map<SubChain, Integer> subChainCountMap) {
        assertTrue(iterator.hasNext());
        SubChain subChain = iterator.next();
        Integer count = subChainCountMap.get(subChain);
        if (count == null) {
            subChainCountMap.put(subChain, 1);
        } else {
            subChainCountMap.put(subChain, count + 1);
        }
    }

}
