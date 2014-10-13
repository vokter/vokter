package pt.ua.adis.argus.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

/**
 * Representation of a user in the application. A user must incorporate and show to
 * the application user the following data: a name and an email.
 *
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>)
 * @version 1.0
 */
public class User implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(User.class);

    private final String name;
    private final String email;


    private User(final String name,
                 final String email) {
        this.name = name;
        this.email = email;
    }


    /**
     * Static constructor that performs validation before instantiating the object.
     * A user is required to have the following data: a name and an email.
     * If the validation is successfully, an object of user
     * is returned. In case contrary, {@link IllegalArgumentException} is thrown.
     *
     * @param name  the name of the user
     * @param email the email of the user
     */
    public static User newInstance(final String name,
                                   final String email) {

        // name must not be null nor empty
        if (name == null || name.isEmpty())
            throw new IllegalArgumentException(
                    "Invalid name! A user must contain a non-null, non-empty name.");

        // email must not be null nor empty
        if (email == null || email.isEmpty() ||
                !Constants.PATTERN_EMAIL.matcher(email).matches())
            throw new IllegalArgumentException("Invalid email! A user must " +
                    "contain a non-null, non-empty email.");

        return new User(name, email);
    }


    /**
     * Returns the name attribute of this user.
     *
     * @return this user's name
     */
    public String getName() {
        return name;
    }


    /**
     * Returns the email attribute of this user.
     *
     * @return this user's email
     */
    public String getEmail() {
        return email;
    }


    @Override
    public String toString() {
        return getEmail();
    }


    /**
     * Checks if this user is equal to the provided user.
     *
     * @param o the provided user to be tested for equality
     * @return <tt>true</tt> if this user is equal to the provided
     * user, and <tt>false</tt> in case contrary
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (!(o instanceof User)) return false;

        User user = (User) o;
        return name.equals(user.name) && email.equals(user.email);

    }


    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + email.hashCode();
        return result;
    }
}
