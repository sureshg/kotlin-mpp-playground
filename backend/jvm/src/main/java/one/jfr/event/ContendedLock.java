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

public class ContendedLock extends Event {
    public final long duration;
    public final int classId;

    public ContendedLock(long time, int tid, int stackTraceId, long duration, int classId) {
        super(time, tid, stackTraceId);
        this.duration = duration;
        this.classId = classId;
    }

    @Override
    public int hashCode() {
        return classId * 127 + stackTraceId;
    }

    @Override
    public boolean sameGroup(Event o) {
        if (o instanceof ContendedLock) {
            ContendedLock c = (ContendedLock) o;
            return classId == c.classId;
        }
        return false;
    }

    @Override
    public long value() {
        return duration;
    }
}
