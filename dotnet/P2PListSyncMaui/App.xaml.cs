using Couchbase.Lite;
using Couchbase.Lite.Logging;
using P2PListSync.Services;
using P2PListSync.Views;

namespace P2PListSync;

public partial class App : Application
{
	public App()
	{
		InitializeComponent();

        // Set true to get CBL Console Logs (Verbose) and additional app debug messages.
        CoreApp.IsDebugging = true;

        if (CoreApp.IsDebugging) {
            Database.Log.Console.Level = LogLevel.Verbose;
        }

        CoreApp.LoadAndInitDB();

        DependencyService.Register<SeasonalDataStore>();

        CoreApp.RequiresUserAuth = true;

        if (CoreApp.RequiresUserAuth) {
            CoreApp.LoadUserAllowListAsync();
            MainPage = new LoginPage();
        } else {
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
