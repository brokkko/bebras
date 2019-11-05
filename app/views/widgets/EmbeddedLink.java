package views.widgets;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;

public class EmbeddedLink extends ResourceLink {
    private static String encode(String code) {
        String base64 = Base64.getEncoder().encodeToString(code.getBytes(StandardCharsets.UTF_8));
        return "data:application/x-javascript;base64," + base64;
    }

    public EmbeddedLink(String code) {
        super(encode(code), "js");
    }

    @Override
    public String apply() {
        return getLocalUrl();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EmbeddedLink that = (EmbeddedLink)o;

        return Objects.equals(this.getLocalUrl(), that.getLocalUrl());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getLocalUrl());
    }
}
