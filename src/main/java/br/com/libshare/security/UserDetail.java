package br.com.libshare.security;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import br.com.libshare.permission.PermissionEntity;
import br.com.libshare.profile.ProfileEntity;
import br.com.libshare.user.UserEntity;
import br.com.libshare.user.UserRepository;

@Component
public class UserDetail implements UserDetailsService {

	@PersistenceContext
	private EntityManager em;
	
	@Autowired
	private UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		UserEntity user = this.userRepository.findByEmail(email);

		if (user == null) {
			throw new UsernameNotFoundException("Usuário com email \"" + email + "\" não encontrado.");
		}
		
		Query profileResult = em.createNativeQuery("SELECT A.* FROM PERFIL A WHERE A.CODUSU = :CODUSU AND A.ATIVO = 'S'", ProfileEntity.class);
		profileResult.setParameter("CODUSU", user.getId());
		ProfileEntity profileEntity =null;
		try {
			profileEntity = (ProfileEntity) profileResult.getSingleResult();
		}catch (NoResultException ignore) {
		}

		String name = profileEntity == null ? "" : profileEntity.getName();
		LoginDetailBean login = new LoginDetailBean(name, user.getEmail(), user.getPassword(), user.getId());

		for (PermissionEntity permission : user.getPermissions()) {
			login.addRole(permission.getRole());
		}

		return login;
	}

}
