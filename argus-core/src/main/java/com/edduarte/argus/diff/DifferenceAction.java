/*
 * Copyright 2015 Ed Duarte
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.edduarte.argus.diff;

/**
 * A structure that set the action that was performed on a text for it to trigger a
 * difference.
 * The following example ...
 * {Diff(Status.deleted, "Hello"), Diff(Status.inserted, "Goodbye"),
 * Diff(Status.nothing, " world.")}
 * ... means that the newer document snapshot erased "Hello", wrote "Goodbye" and
 * kept " world.".
 *
 * @author Ed Duarte (<a href="mailto:ed@edduarte.com">ed@edduarte.com</a>)
 * @version 1.3.2
 * @since 1.0.0
 */
public enum DifferenceAction {
    inserted, deleted, nothing
}
