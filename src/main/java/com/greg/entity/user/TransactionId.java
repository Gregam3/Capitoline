package com.greg.entity.user;

import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * @author Greg Mitten (i7676925)
 * gregoryamitten@gmail.com
 */
@Embeddable
public class TransactionId implements Serializable {
    private String email;
    private String acronym;
    private int transactionNumber;

    public TransactionId(String email, String acronym, int transactionNumber) {
        this.email = email;
        this.acronym = acronym;
        this.transactionNumber = transactionNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TransactionId)) return false;

        TransactionId that = (TransactionId) o;

        return transactionNumber == that.transactionNumber &&
                (email != null ? email.equals(that.email) : that.email == null) &&
                (acronym != null ? acronym.equals(that.acronym) : that.acronym == null);
    }

    @Override
    public int hashCode() {
        int result = email != null ? email.hashCode() : 0;
        result = 31 * result + (acronym != null ? acronym.hashCode() : 0);
        result = 31 * result + transactionNumber;
        return result;
    }
}
