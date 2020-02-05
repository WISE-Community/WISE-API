import {Component, OnInit} from '@angular/core';
import {TeacherService} from "../../../../../../site/src/app/teacher/teacher.service";
import {TaskService} from "../../core/services/data/task.service";

@Component({
  selector: 'app-classes-page',
  templateUrl: './classes-page.component.html',
  styleUrls: ['./classes-page.component.scss']
})
export class ClassesPageComponent implements OnInit {

  constructor(private tasksService: TaskService) {
      let allTasks = this.tasksService.getAllTasks();
      console.log(allTasks);
  }

  ngOnInit() {
  }

}
