package com.naz013.tree;

import java.util.List;

/**
 * Copyright 2017 Nazar Suhovich
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public interface TreeInterface<I, K, V extends TreeObject<I, K>>  {

    void add(V v);

    void remove(K[] k);

    void remove(V v);

    List<V> get(K[] k);

    int size();

    void remove(I i);

    V get(I i);

    boolean contains(V v);

    void clear();
}