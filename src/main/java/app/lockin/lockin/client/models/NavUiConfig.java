package app.lockin.lockin.client.models;

public class NavUiConfig {
    private final boolean showNavBar;
    private final String title;
    private final boolean showSearchBar;
    private final boolean showRefreshButton;
    private final boolean showSettingsButton;

    public NavUiConfig(
            boolean showNavBar,
            String title,
            boolean showSearchBar,
            boolean showRefreshButton,
            boolean showSettingsButton
    ) {
        this.showNavBar = showNavBar;
        this.title = title;
        this.showSearchBar = showSearchBar;
        this.showRefreshButton = showRefreshButton;
        this.showSettingsButton = showSettingsButton;
    }

    public boolean isShowNavBar() {
        return showNavBar;
    }

    public String getTitle() {
        return title;
    }

    public boolean isShowSearchBar() {
        return showSearchBar;
    }

    public boolean isShowRefreshButton() {
        return showRefreshButton;
    }

    public boolean isShowSettingsButton() {
        return showSettingsButton;
    }
}
