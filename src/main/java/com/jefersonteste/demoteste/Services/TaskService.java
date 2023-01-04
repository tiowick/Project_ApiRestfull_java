package com.jefersonteste.demoteste.Services;

import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jefersonteste.demoteste.Repositories.TaskRepository;
import com.jefersonteste.demoteste.Security.UserSpringSecurity;
import com.jefersonteste.demoteste.Services.exceptions.AuthorizationException;
import com.jefersonteste.demoteste.Services.exceptions.DataBindingViolationException;
import com.jefersonteste.demoteste.Services.exceptions.ObjectNotFoundException;
import com.jefersonteste.demoteste.models.Task;
import com.jefersonteste.demoteste.models.User;
import com.jefersonteste.demoteste.models.enums.ProfileEnum;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserService userService;

    public Task findByTask(Long id) {

        Task task = this.taskRepository.findById(id).orElseThrow(() -> new ObjectNotFoundException(
            "Tarefa não encontrada! Id: " + id + ", Tipo: " + Task.class.getName()));

        UserSpringSecurity userSpringSecurity = UserService.authenticated();
        if(Objects.isNull(userSpringSecurity) 
                || !userSpringSecurity.hasRole(ProfileEnum.ADMIN) && !userHasTask(userSpringSecurity, task)){
            throw new AuthorizationException("Acesso negado!");
        }
        return task;

    }

    public List<Task> findAllByUser() {
        UserSpringSecurity userSpringSecurity = UserService.authenticated();
        if(Objects.isNull(userSpringSecurity)){
            throw new AuthorizationException("Acesso negado!");
        }
        List<Task> tasks = this.taskRepository.findByUser_Id(userSpringSecurity.getId());
        return tasks;
    }

    @Transactional
    public Task create(Task obj) {
            UserSpringSecurity userSpringSecurity = UserService.authenticated();
            if(Objects.isNull(userSpringSecurity)){
                throw new AuthorizationException("Acesso negado!");
            }
        User user = this.userService.findById(userSpringSecurity.getId()); //obj.getUser().getId()

        obj.setId(null);
        obj.setUser(user);
        obj = this.taskRepository.save(obj);
        return obj;

    }

    public Task update(Task obj) {
        Task newObjt = findByTask(obj.getId());
        newObjt.setDescription(obj.getDescription());
        return this.taskRepository.save(newObjt);
    }

    public void delete(Long id) {
        findByTask(id);
        try {
            this.taskRepository.deleteById(id);
        } catch (Exception e) {
            throw new DataBindingViolationException("Não é possivel excluir pois há entidades relacionadas!");
        }
    }

    private Boolean userHasTask(UserSpringSecurity userSpringSecurity, Task task){
        return task.getUser().getId().equals(userSpringSecurity.getId());

    }

}
