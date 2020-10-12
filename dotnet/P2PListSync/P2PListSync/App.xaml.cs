using Xamarin.Forms;
using Xamarin.Forms.Xaml;
using P2PListSync.Views;

[assembly: XamlCompilation(XamlCompilationOptions.Compile)]
namespace P2PListSync
{
    public partial class App : Application
    {
        public static bool IsUserLoggedIn { get; set; }

        public App()
        {
            InitializeComponent();

            CoreApp.LoadUserAllowList();
            if (!IsUserLoggedIn) {
                MainPage = new LoginPage();
            } else {
                CoreApp.LoadAndInitDB();
                MainPage = new MainPage();
            }
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
