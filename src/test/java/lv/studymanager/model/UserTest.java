package lv.studymanager.model;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class UserTest {

    @Test
    void getFullName_returnsFirstAndLastName() {
        User user = User.builder()
            .firstName("Jānis")
            .lastName("Bērziņš")
            .build();
        assertThat(user.getFullName()).isEqualTo("Jānis Bērziņš");
    }

    @Test
    void defaultRole_isStudent() {
        User user = User.builder()
            .firstName("A").lastName("B")
            .email("a@b.lv").passwordHash("hash")
            .build();
        assertThat(user.getRole()).isEqualTo(User.Role.STUDENT);
    }
}
