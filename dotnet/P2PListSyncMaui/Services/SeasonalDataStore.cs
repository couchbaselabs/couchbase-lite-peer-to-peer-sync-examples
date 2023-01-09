//
// SeasonalItemsPage.cs
//
// Author:
// 	Sandy Chuang
//
//  Copyright © 2023 Couchbase Inc. All rights reserved.
//
//

using Couchbase.Lite;
using P2PListSync.Models;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace P2PListSync.Services
{
    public class SeasonalDataStore : IDataStore<SeasonalItem>
    {
        private Collection _col = CoreApp.COLL;
        private readonly List<SeasonalItem> _items;

        public SeasonalDataStore()
        {
            _items = new List<SeasonalItem>();
            using (var dict = _col.GetDocument(CoreApp.DocId)) {
                var arr = dict.GetArray(CoreApp.ArrKey);
                for (int i = 0; i < arr.Count; i++) {
                    DictionaryObject d = arr.GetDictionary(i);
                    SeasonalItem item = new SeasonalItem { Name = d.GetString("key"), Quantity = d.GetInt("value"), ImageByteArray = d.GetBlob("image")?.Content };
                    _items.Add(item);
                }
            }
        }

        public async Task<bool> AddItemAsync(SeasonalItem item)
        {
            _items.Add(item);

            return await Task.FromResult(true);
        }

        public async Task<bool> DeleteItemAsync(string name)
        {
            var oldItem = _items.Where((SeasonalItem arg) => arg.Name == name).FirstOrDefault();
            _items.Remove(oldItem);

            return await Task.FromResult(true);
        }

        public async Task<SeasonalItem> GetItemAsync(string name)
        {
            return await Task.FromResult(_items.FirstOrDefault(s => s.Name == name));
        }

        public async Task<IEnumerable<SeasonalItem>> GetItemsAsync(bool forceRefresh = false)
        {
            return await Task.FromResult(_items);
        }

        public async Task<bool> UpdateItemAsync(SeasonalItem item)
        {
            var oldItem = _items.Where((SeasonalItem arg) => arg.Name == item.Name).FirstOrDefault();
            _items.Remove(oldItem);
            _items.Add(item);

            return await Task.FromResult(true);
        }
    }
}
