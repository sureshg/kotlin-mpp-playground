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

public class CPULoad extends Event {
    public final float jvmUser;
    public final float jvmSystem;
    public final float machineTotal;

    public CPULoad(JfrReader jfr) {
        super(jfr.getVarlong(), 0, 0);
        this.jvmUser = jfr.getFloat();
        this.jvmSystem = jfr.getFloat();
        this.machineTotal = jfr.getFloat();
    }
}
