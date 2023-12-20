/*
 * Copyright 2021 Andrei Pangin
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

public class AllocationSample extends Event {
    public final int classId;
    public final long allocationSize;
    public final long tlabSize;

    public AllocationSample(long time, int tid, int stackTraceId, int classId, long allocationSize, long tlabSize) {
        super(time, tid, stackTraceId);
        this.classId = classId;
        this.allocationSize = allocationSize;
        this.tlabSize = tlabSize;
    }

    @Override
    public int hashCode() {
        return classId * 127 + stackTraceId + (tlabSize == 0 ? 17 : 0);
    }

    @Override
    public boolean sameGroup(Event o) {
        if (o instanceof AllocationSample) {
            AllocationSample a = (AllocationSample) o;
            return classId == a.classId && (tlabSize == 0) == (a.tlabSize == 0);
        }
        return false;
    }

    @Override
    public long value() {
        return tlabSize != 0 ? tlabSize : allocationSize;
    }
}
