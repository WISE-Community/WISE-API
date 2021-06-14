import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { InstructorPageRoutingModule } from './instructor-page-routing.module';
import { InstructorPageComponent } from './instructor-page.component';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';

@NgModule({
    declarations: [InstructorPageComponent],
    imports: [
        CommonModule,
        InstructorPageRoutingModule,
        MatTableModule,
        MatButtonModule,
    ],
})
export class InstructorPageModule {}
