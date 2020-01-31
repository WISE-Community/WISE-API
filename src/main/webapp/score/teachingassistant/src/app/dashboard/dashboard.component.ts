import {Component, OnInit} from '@angular/core';
import {map} from 'rxjs/operators';
import {BreakpointObserver, Breakpoints} from '@angular/cdk/layout';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {
  breakpoint: number;
  /** Based on the screen size, switch from standard to one column per row */
  cards = this.breakpointObserver.observe(Breakpoints.Handset).pipe(
    map(({ matches }) => {
      return [
        { title: 'Active Students', students: ['Jack', 'Bob', 'Will'], cols: 1, rows: 1 },
        { title: 'Need Assistance', students: ['Dina', 'Sue', 'Sally', 'Joe'], cols: 1, rows: 1 },
        { title: 'Need Approval', students: ['Ed', 'Billy'], cols: 1, rows: 1 },
      ];
    })
  );

  constructor(private breakpointObserver: BreakpointObserver) {}

  ngOnInit() {
    this.breakpoint = (window.innerWidth <= 500) ? 1 : 3;
  }

  onResize(event) {
    this.breakpoint = (event.target.innerWidth <= 500) ? 1 : 3;
    console.log(this.breakpoint, event.target.innerWidth);
  }

}
