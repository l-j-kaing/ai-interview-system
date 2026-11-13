package known.my.pojo.enums;

public enum StorageTypeEnum {

    LOCAL("local"),
    MINIO("minio"),
    OSS("oss");

    private final String value;

    StorageTypeEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}