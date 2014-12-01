package in.xebia.xebiacaller.contactfetchers;

/**
 * Created by anujmalhotra on 28/11/14.
 */
public final class PhoneFetcher {

    public static final String PhoneBookContactsFetcher = "phonebook";
    public static final String XebiaContactsFetcher = "xebiacontacts";

    public static ContactFetcher getFetcher(String type) {
        if (type == null) {
            return null;
        }
        if (type.equalsIgnoreCase(PhoneBookContactsFetcher)) {
            return new in.xebia.xebiacaller.contactfetchers.PhoneBookContactsFetcher();
        } else if (type.equalsIgnoreCase(XebiaContactsFetcher)) {
            return new in.xebia.xebiacaller.contactfetchers.XebiaContactsFetcher();
        }
        return null;
    }

}
