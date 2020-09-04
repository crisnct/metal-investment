package com.investment.metal.service;

import com.investment.metal.Util;
import com.investment.metal.database.Customer;
import com.investment.metal.database.CustomerRepository;
import com.investment.metal.database.Login;
import com.investment.metal.database.LoginRepository;
import com.investment.metal.exceptions.BusinessException;
import com.investment.metal.exceptions.CustomErrorCodes;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service("customerService")
public class DefaultCustomerService implements CustomerService {

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    LoginRepository loginRepository;

    @Autowired
    private EmailService emailService;

    @Override
    public Customer registerNewUser(String username, String password, String email) throws BusinessException {
        //TODO validate email and throw exception if case
        String token = this.login(username, password);
        if (StringUtils.isNotBlank(token)) {
            throw new BusinessException(CustomErrorCodes.USER_ALREADY_EXISTS, "There user " + username + " is already registered in the database");
        }
        final Customer user = new Customer();
        user.setUsername(username);
        user.setPassword(password);
        user.setEmail(email);
        return this.customerRepository.save(user);
    }

    @Override
    public void validateAccount(Customer user) {
        final int codeGenerated = 1000000 + Util.getRandomGenerator().nextInt(999999);
        final String emailContent = "<strong>this</strong> is <b>subj</b>ect" + codeGenerated;
        this.emailService.sendMail(user.getEmail(), "Testing from Spring Boot", emailContent);
    }

    @Override
    public String login(String username, String password) {
        Optional<Customer> customer = customerRepository.findByUsernameAndPassword(username, password);
        if (customer.isPresent()) {


        } else {

        }
        return StringUtils.EMPTY;
    }

    public void validateCode(String username, int code) {
        String token = UUID.randomUUID().toString();
//        Customer custom = customer.get();
//        custom.setToken(token);
//        customerRepository.save(custom);
    }

    @Override
    public Optional<User> findByToken(String token) {
        Optional<Login> login = loginRepository.findByToken(token);
        if (login.isPresent()) {
            Customer customer1 = customerRepository.getOne(login.get().getId());
            User user = new User(customer1.getUsername(), customer1.getPassword(), true, true, true, true,
                    AuthorityUtils.createAuthorityList("USER"));
            return Optional.of(user);
        }
        return Optional.empty();
    }

    @Override
    public Customer findById(Long id) {
        Optional<Customer> customer = customerRepository.findById(id);
        return customer.orElse(null);
    }
}
