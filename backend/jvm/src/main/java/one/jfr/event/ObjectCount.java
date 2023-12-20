/*
 * Copyright 2023 Andrei Pangin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package one.jfr.event;

import one.jfr.JfrReader;

public class ObjectCount extends Event {
    public final int gcId;
    public final int classId;
    public final long count;
    public final long totalSize;

    public ObjectCount(JfrReader jfr) {
        super(jfr.getVarlong(), 0, 0);
        this.gcId = jfr.getVarint();
        this.classId = jfr.getVarint();
        this.count = jfr.getVarlong();
        this.totalSize = jfr.getVarlong();
    }
}
