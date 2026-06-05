package com.pedrohenrique.pagcontrolback.model;

import com.pedrohenrique.pagcontrolback.exceptions.InvalidSupplierCnpjException;
import com.pedrohenrique.pagcontrolback.exceptions.SupplierNameRequiredException;
import com.pedrohenrique.pagcontrolback.exceptions.UserRequiredException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SupplierTest {

    @Nested
    class ConstructorTests {

        @Test
        void shouldCreateSupplierWithOnlyName() {
            Supplier supplier = new Supplier(
                    "Supplier A",
                    null,
                    new User()
            );

            assertNotNull(supplier);
            assertEquals("Supplier A", supplier.getName());
            assertNull(supplier.getCnpj());
            assertTrue(supplier.getActive());
            assertNotNull(supplier.getCreatedAt());
        }

        @Test
        void shouldCreateSupplierWithValidCnpj() {
            Supplier supplier = new Supplier(
                    "Supplier B",
                    "12.345.678/0001-95",
                    new User()
            );

            assertNotNull(supplier);
            assertEquals("Supplier B", supplier.getName());
            assertEquals(
                    "12345678000195",
                    supplier.getCnpj().value()
            );
            assertTrue(supplier.getActive());
        }

        @Test
        void shouldAcceptNullCnpj() {
            Supplier supplier = new Supplier(
                    "Supplier C",
                    null,
                    new User()
            );

            assertNull(supplier.getCnpj());
        }

        @Test
        void shouldAcceptBlankCnpj() {
            Supplier supplier = new Supplier(
                    "Supplier D",
                    "   ",
                    new User()
            );

            assertNull(supplier.getCnpj());
        }

        @Test
        void shouldRemoveFormattingFromCnpj() {
            Supplier supplier = new Supplier(
                    "Supplier E",
                    "12.345.678/0001-95",
                    new User()
            );

            assertEquals(
                    "12345678000195",
                    supplier.getCnpj().value()
            );
        }

        @Test
        void shouldThrowWhenNameIsNull() {
            SupplierNameRequiredException exception =
                    assertThrows(
                            SupplierNameRequiredException.class,
                            () -> new Supplier(
                                    null,
                                    null,
                                    new User()
                            )
                    );

            assertEquals(
                    "Supplier name cannot be null or empty.",
                    exception.getMessage()
            );
        }

        @Test
        void shouldThrowWhenNameIsBlank() {
            SupplierNameRequiredException exception =
                    assertThrows(
                            SupplierNameRequiredException.class,
                            () -> new Supplier(
                                    "   ",
                                    null,
                                    new User()
                            )
                    );

            assertEquals(
                    "Supplier name cannot be null or empty.",
                    exception.getMessage()
            );
        }

        @Test
        void shouldThrowWhenCnpjIsInvalid() {
            InvalidSupplierCnpjException exception =
                    assertThrows(
                            InvalidSupplierCnpjException.class,
                            () -> new Supplier(
                                    "Supplier F",
                                    "invalid-cnpj",
                                    new User()
                            )
                    );

            assertEquals(
                    "Invalid CNPJ",
                    exception.getMessage()
            );
        }

        @Test
        void shouldThrowWhenUserIsNull() {
            assertThrows(
                    UserRequiredException.class,
                    () -> new Supplier(
                            "Supplier G",
                            null,
                            null
                    )
            );
        }
    }

    @Nested
    class SetUserTests {

        @Test
        void shouldSetUserSuccessfully() {
            Supplier supplier = new Supplier(
                    "Supplier H",
                    null,
                    new User()
            );

            User user = new User();

            supplier.setUser(user);

            assertEquals(user, supplier.getUser());
        }

        @Test
        void shouldThrowWhenSettingNullUser() {
            Supplier supplier = new Supplier(
                    "Supplier I",
                    null,
                    new User()
            );

            assertThrows(
                    UserRequiredException.class,
                    () -> supplier.setUser(null)
            );
        }
    }

    @Nested
    class SetCnpjTests {

        @Test
        void shouldSetValidCnpj() {
            Supplier supplier = new Supplier(
                    "Supplier J",
                    null,
                    new User()
            );

            supplier.setCnpj("12.345.678/0001-95");

            assertEquals(
                    "12345678000195",
                    supplier.getCnpj().value()
            );
        }

        @Test
        void shouldIgnoreNullCnpj() {
            Supplier supplier = new Supplier(
                    "Supplier K",
                    null,
                    new User()
            );

            supplier.setCnpj(null);

            assertNull(supplier.getCnpj());
        }

        @Test
        void shouldIgnoreBlankCnpj() {
            Supplier supplier = new Supplier(
                    "Supplier L",
                    null,
                    new User()
            );

            supplier.setCnpj("   ");

            assertNull(supplier.getCnpj());
        }

        @Test
        void shouldThrowWhenSettingInvalidCnpj() {
            Supplier supplier = new Supplier(
                    "Supplier M",
                    null,
                    new User()
            );

            assertThrows(
                    InvalidSupplierCnpjException.class,
                    () -> supplier.setCnpj("invalid")
            );
        }
    }
}