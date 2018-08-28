package uk.ac.susx.tag.method51.twitter;

/*
 * #%L
 * LabelDecision.java - method51 - University of Sussex - 2,013
 * %%
 * Copyright (C) 2013 - 2014 University of Sussex
 * %%
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
 * #L%
 */


/**
 * Created with IntelliJ IDEA.
 * User: sw206
 * Date: 31/10/2012
 * Time: 18:27
 * To change this template use File | Settings | File Templates.
 */
public class LabelDecision {

    public String[] labels;
    public int labelIdx;
    public String label;
    public double[] likelihoods;

    @Override
    public String toString() {
        return label;
    }


    public LabelDecision labels(String[] labels) {
        this.labels = labels;
        return this;
    }

    public LabelDecision labelIdx(int labelIdx) {
        this.labelIdx = labelIdx;
        return this;
    }

    public LabelDecision label(String label) {
        this.label = label;
        return this;
    }

    public LabelDecision likelihoods(double[] likelihoods) {
        this.likelihoods = likelihoods;
        return this;
    }


}
