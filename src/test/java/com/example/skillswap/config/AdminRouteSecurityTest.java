package com.example.skillswap.config;

import com.example.skillswap.config.JwtUtils;
import com.example.skillswap.service.AccountStatusService;
import com.example.skillswap.service.AdminUserService;
import com.example.skillswap.service.CustomUserDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(com.example.skillswap.controller.AdminUserController.class)
@Import(SecurityConfig.class)
class AdminRouteSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @MockitoBean
    private JwtUtils jwtUtils;

    @MockitoBean
    private AccountStatusService accountStatusService;

    @MockitoBean
    private AdminUserService adminUserService;

    @Test
    @WithMockUser(roles = "USER")
    void regularUserCannotAccessAdminRoutes() throws Exception {
        mockMvc.perform(get("/api/admin/users/42"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(adminUserService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanAccessAdminRoutes() throws Exception {
        mockMvc.perform(get("/api/admin/users/42"))
                .andExpect(status().isOk());

        verify(adminUserService).getUserDetails(42L);
    }
}
