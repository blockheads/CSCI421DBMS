package storagemanager.buffermanager.page;

public enum PageTypes {
    RECORD_PAGE(RecordPage.class, "pages/"),
    INDEX_PAGE(IndexPage.class, "index/");

    public final Class<? extends Page> pageClass;
    public final String relLoc;

    PageTypes(Class<? extends Page> pageClass, String relLoc) {
        this.pageClass = pageClass;
        this.relLoc = relLoc;
    }
}
