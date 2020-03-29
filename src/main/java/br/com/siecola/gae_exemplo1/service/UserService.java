package br.com.siecola.gae_exemplo1.service;

import br.com.siecola.gae_exemplo1.model.User;
import br.com.siecola.gae_exemplo1.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service("userDetailsService")
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<User> optUser =
                userRepository.getByEmail(email);
        if (optUser.isPresent()) {
            userRepository.updateUserLogin(optUser.get());
            return optUser.get();
        } else {
            throw new UsernameNotFoundException("Usuário não encontrado");
        }
    }
}