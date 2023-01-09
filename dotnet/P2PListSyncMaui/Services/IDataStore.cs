//
// SeasonalItemsPage.cs
//
// Author:
// 	Sandy Chuang
//
//  Copyright © 2023 Couchbase Inc. All rights reserved.
//
//

using System.Collections.Generic;
using System.Threading.Tasks;

namespace P2PListSync.Services
{
    public interface IDataStore<T>
    {
        Task<bool> AddItemAsync(T item);
        Task<bool> UpdateItemAsync(T item);
        Task<bool> DeleteItemAsync(string id);
        Task<T> GetItemAsync(string id);
        Task<IEnumerable<T>> GetItemsAsync(bool forceRefresh = false);
    }
}
