package com.greg.entity.user;

import javax.persistence.Embeddable;

/**
 * @author Greg Mitten (i7676925)
 * gregoryamitten@gmail.com
 */
@Embeddable
public class HoldingId {
    private String email;
    private String acronym;

    public HoldingId(String email, String acronym) {
        this.email = email;
        this.acronym = acronym;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HoldingId)) return false;

        HoldingId holdingId = (HoldingId) o;

        return email.equals(holdingId.email) && acronym.equals(holdingId.acronym);
    }

    @Override
    public int hashCode() {
        int result = email.hashCode();
        result = 31 * result + acronym.hashCode();
        return result;
    }
}
