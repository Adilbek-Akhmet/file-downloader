package sp.service.impl;

import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import sp.repository.FileRepository;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserDetailsService {

    private final FileRepository fileRepository;

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        return fileRepository.findByUsername(login)
                .orElseThrow(() -> new UsernameNotFoundException("пользователь не существует"));
    }
}


