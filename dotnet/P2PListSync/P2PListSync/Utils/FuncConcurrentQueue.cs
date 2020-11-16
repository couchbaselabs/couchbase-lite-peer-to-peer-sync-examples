//
// FuncConcurrentQueue.cs
//
// Author:
// 	Sandy Chuang
//
//  Copyright © 2020 Couchbase Inc. All rights reserved.
//

using System;
using System.Collections.Generic;

namespace P2PListSync.Utils
{
    class FuncConcurrentQueue<T>
    {
        private readonly Func<T> _factory;
        private readonly Queue<T> _pool;

        public FuncConcurrentQueue(Func<T> factory)
        {
            _factory = factory;
            _pool = new Queue<T>();
        }

        public int Count
        {
            get { return _pool.Count; }
        }

        public void Add(T item)
        {
            if (item == null)
                return;
            lock (_pool)
            {
                _pool.Enqueue(item);
            }
        }

        public T Take()
        {
            lock (_pool)
            {
                return _pool.Count > 0 ? _pool.Dequeue() : _factory();
            }
        }
    }
}
