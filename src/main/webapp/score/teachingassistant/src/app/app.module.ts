import {BrowserModule} from '@angular/platform-browser';
import {MissingTranslationStrategy, NgModule} from '@angular/core';

import {AppRoutingModule} from './app-routing.module';
import {AppComponent} from './app.component';
import {NavModule} from './core/components/nav/nav.module';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {HTTP_INTERCEPTORS, HttpClientModule} from '@angular/common/http';
import {HttpConfigInterceptor} from './core/services/http/interceptor.service';
import {MatIconModule, MatTableModule} from '@angular/material';
import {DashboardComponent} from "./dashboard/dashboard.component";
import {MainNavComponent} from "./main-nav/main-nav.component";
import {HttpHandler, HttpInterceptor, HttpRequest} from '@angular/common/http';
import {Injectable}  from '@angular/core';
import {LayoutModule} from '@angular/cdk/layout';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MatAutocompleteModule} from '@angular/material/autocomplete';
import {MatButtonModule} from '@angular/material/button';
import {MatCardModule} from '@angular/material/card';
import {MatCheckboxModule} from '@angular/material/checkbox';
import {MatNativeDateModule} from '@angular/material/core';
import {MatDatepickerModule} from '@angular/material/datepicker';
import {MatDialogModule} from '@angular/material/dialog';
import {MatExpansionModule} from '@angular/material/expansion';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatGridListModule} from '@angular/material/grid-list';
import {MatInputModule} from '@angular/material/input';
import {MatListModule} from '@angular/material/list';
import {MatMenuModule} from '@angular/material/menu';
import {MatPaginatorModule} from '@angular/material/paginator';
import {MatRadioModule} from '@angular/material/radio';
import {MatSelectModule} from '@angular/material/select';
import {MatSidenavModule} from '@angular/material/sidenav';
import {MAT_SNACK_BAR_DEFAULT_OPTIONS, MatSnackBarModule} from '@angular/material/snack-bar';
import {MatSortModule} from '@angular/material/sort';
import {MatStepperModule} from '@angular/material/stepper';
import {MatTabsModule} from '@angular/material/tabs';
import {MatToolbarModule} from '@angular/material/toolbar';
import {MatTooltipModule} from '@angular/material/tooltip';
import { AuthGuard } from './auth/auth.guard';
import {ConfigService} from "../../../../site/src/app/services/config.service";
import {RunService} from "./core/services/data/run.service";
import {TeacherService} from "../../../../site/src/app/teacher/teacher.service";
import {HttpErrorInterceptor} from "../../../../site/src/app/http-error.interceptor";
import {I18n, MISSING_TRANSLATION_STRATEGY} from "@ngx-translate/i18n-polyfill";
@NgModule({
    declarations: [
        AppComponent,
        DashboardComponent,
        MainNavComponent
    ],
    imports: [
        BrowserModule,
        BrowserAnimationsModule,
        AppRoutingModule,
        NavModule,
        HttpClientModule,
        MatIconModule,
        MatTableModule,
        MatToolbarModule,
        MatButtonModule,
        MatSidenavModule,
        MatIconModule,
        MatListModule,
        MatTableModule,
        MatSortModule,
        MatPaginatorModule,
        MatFormFieldModule,
        MatPaginatorModule,
        MatSortModule,
        MatInputModule,
        MatAutocompleteModule,
        MatTooltipModule,
        MatCardModule,
        MatDialogModule,
        MatSnackBarModule,
        MatSelectModule,
        MatTabsModule,
        FormsModule,
        MatCheckboxModule,
        MatExpansionModule,
        ReactiveFormsModule,
        MatRadioModule,
        HttpClientModule,
        MatStepperModule,
        LayoutModule,
        MatGridListModule,
        MatMenuModule,
    ],
    providers: [
        {
            provide: HTTP_INTERCEPTORS,
            useClass: HttpErrorInterceptor,
            multi: true
        },
        { provide: MISSING_TRANSLATION_STRATEGY, useValue: MissingTranslationStrategy.Ignore },

        I18n,
        // {
        //     provide: HTTP_INTERCEPTORS,
        //     useClass: HttpConfigInterceptor,
        //     multi: true,
        // },
        AuthGuard,
        ConfigService,
        TeacherService,
        RunService
    ],
    bootstrap: [AppComponent],
    exports: [

    ]
})
export class AppModule {}
