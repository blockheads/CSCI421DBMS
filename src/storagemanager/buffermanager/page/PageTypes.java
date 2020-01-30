package storagemanager.buffermanager.page;

public enum PageTypes {
    RECORD_PAGE(RecordPage.class),
    INDEX_PAGE(IndexPage.class);

    public final Class<? extends Page> pageClass;

    PageTypes(Class<? extends Page> pageClass) {
        this.pageClass = pageClass;
    }
}
