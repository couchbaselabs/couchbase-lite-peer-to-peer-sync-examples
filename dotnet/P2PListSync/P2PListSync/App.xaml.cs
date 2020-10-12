using Xamarin.Forms;
using Xamarin.Forms.Xaml;
using P2PListSync.Views;
using P2PListSync.ViewModels;

[assembly: XamlCompilation(XamlCompilationOptions.Compile)]
namespace P2PListSync
{
    public partial class App : Application
    {
        public App()
        {
            InitializeComponent();

            CoreApp.LoadAndInitDB();
            
            MainPage = new MainPage();
        }

        protected override void OnStart()
        {
            // Handle when your app starts
        }

        protected override void OnSleep()
        {
            // Handle when your app sleeps
        }

        protected override void OnResume()
        {
            // Handle when your app resumes
        }
    }
}
