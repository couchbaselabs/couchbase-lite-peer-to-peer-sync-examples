// SeasonalItemsViewModel.cs
//
// Author:
// 	Sandy Chuang
//
//  Copyright © 2020 Couchbase Inc. All rights reserved.
//

using System.Threading.Tasks;

using Xamarin.Forms;

using P2PListSync.Models;
using P2PListSync.Services;
using System.Collections.Generic;
using Couchbase.Lite;
using Couchbase.Lite.Query;
using P2PListSync.Utils;
using System.IO;

namespace P2PListSync.ViewModels
{
    public class SeasonalItemsViewModel : BaseViewModel
    {
        private Database _db = CoreApp.DB;

        public HashSet<int> DocsChangeIndexes { get; set; }

        public Command SaveDocumentsCommand { get; set; }

        ObservableConcurrentDictionary<int, SeasonalItem> _items = new ObservableConcurrentDictionary<int, SeasonalItem>();
        public ObservableConcurrentDictionary<int, SeasonalItem> Items
        {
            get { return _items; }
            set {
                _items = value;
                OnPropertyChanged("Items");
            }
        }

        public SeasonalItemsViewModel()
        {
            Title = "What's in Season?";
            Items = new ObservableConcurrentDictionary<int, SeasonalItem>();
            DocsChangeIndexes = new HashSet<int>();
            SaveDocumentsCommand = new Command(async () => await ExecuteSaveDocumentsCommand());

            //tag::LoadData[]
            var q = QueryBuilder.Select(SelectResult.All())
                .From(DataSource.Database(_db))
                .Where(Meta.ID.EqualTo(Expression.String(CoreApp.DocId)))
                .AddChangeListener((sender, args) =>
                {
                    var allResult = args.Results.AllResults();
                    var result = allResult[0];
                    var dict = result[CoreApp.DB.Name].Dictionary;
                    var arr = dict.GetArray(CoreApp.ArrKey);

                    if (arr.Count < Items.Count)
                        Items = new ObservableConcurrentDictionary<int, SeasonalItem>();

                    Parallel.For(0, arr.Count, i =>
                    {
                        var item = arr[i].Dictionary;
                        var name = item.GetString("key");
                        var cnt = item.GetInt("value");
                        var image = item.GetBlob("image");
                        var img = ImageSource.FromStream(() =>
                        {
                            return image?.ContentStream;
                        }) ?? Image("default_food.png");

                        if (_items.ContainsKey(i)) {
                            _items[i].Name = name;
                            _items[i].Quantity = cnt;
                            _items[i].ImageByteArray = image?.Content;
                        } else {
                            var seasonalItem = new SeasonalItem {
                                Index = i,
                                Name = name,
                                Quantity = cnt,
                                ImageByteArray = image?.Content
                            };

                            _items.Add(i, seasonalItem);
                        }

                    });
                });
            //end::LoadData[]
        }

        private async Task<bool> ExecuteSaveDocumentsCommand()
        {
            if (DocsChangeIndexes.Count > 0) {
                using (var doc = _db.GetDocument(CoreApp.DocId))
                using (var mdoc = doc.ToMutable())
                using (var listItems = mdoc.GetArray(CoreApp.ArrKey)) {
                    foreach (var index in DocsChangeIndexes) {
                        var item = Items[index];
                        var dictObj = listItems.GetDictionary(item.Index);
                        dictObj.SetString("key", item.Name);
                        dictObj.SetInt("value", item.Quantity);
                        var blob = new Blob("image/png", item.ImageByteArray);
                        dictObj.SetBlob("image", blob);
                    }

                    _db.Save(mdoc);
                }

                DocsChangeIndexes.Clear();
            }

            return await Task.FromResult(true);
        }

        public async Task ExecuteImageChangedCommand(int index)
        {
            Stream stream = await DependencyService.Get<IPhotoPickerService>().GetImageStreamAsync();
            if (stream == null) {
                return;
            }

            var item = _items[index];
            using (var memoryStream = new MemoryStream()) {
                stream.CopyTo(memoryStream);
                item.ImageByteArray = memoryStream.ToArray();
            }

            DocsChangeIndexes.Add(item.Index);
        }

        public async Task<bool> AddItemAsync(SeasonalItem item)
        {
            using (var doc = _db.GetDocument(CoreApp.DocId))
            using (var mdoc = doc.ToMutable())
            using (MutableArrayObject listItems = mdoc.GetArray(CoreApp.ArrKey)) {
                var dictObj = new MutableDictionaryObject();
                dictObj.SetString("key", item.Name);
                dictObj.SetInt("value", item.Quantity);
                var blob = new Blob("image/png", item.ImageByteArray);
                dictObj.SetBlob("image", blob);

                listItems.AddDictionary(dictObj);
                _db.Save(mdoc);

                _items.Add(_items.Count + 1, item);
            }

            return await Task.FromResult(true);
        }

        public async Task<bool> UpdateItemAsync(SeasonalItem item)
        {
            using (var doc = _db.GetDocument(CoreApp.DocId))
            using (var mdoc = doc.ToMutable())
            using (MutableArrayObject listItems = mdoc.GetArray(CoreApp.ArrKey)) {
                var dictObj = listItems.GetDictionary(item.Index);
                dictObj.SetString("key", item.Name);
                dictObj.SetInt("value", item.Quantity);
                var blob = new Blob("image/png", item.ImageByteArray);
                dictObj.SetBlob("image", blob);

                _db.Save(mdoc);
            }

            return await Task.FromResult(true);
        }

        public async Task<bool> DeleteItemAsync(int index)
        {
            using (var doc = _db.GetDocument(CoreApp.DocId))
            using (var mdoc = doc.ToMutable())
            using (MutableArrayObject listItems = mdoc.GetArray(CoreApp.ArrKey)) {
                listItems.RemoveAt(index);
                _db.Save(mdoc);
            }

            _items.Remove(index);

            return await Task.FromResult(true);
        }

        public ImageSource Image(string IconSource)
        {
            return ImageSource.FromResource(string.Format("P2PListSync.Assets.{0}", IconSource));
        }
    }
}