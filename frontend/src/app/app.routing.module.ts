import { NgModule} from '@angular/core';
import { RouterModule, Routes} from '@angular/router';
import { HomeComponent } from './components/home/home.component';
import { AuthentificationComponent } from './components/authentification/authentification.component';
const routes: Routes = [
    {path : '', redirectTo: '/home', pathMatch: 'full'},
    {path : 'home', component: HomeComponent},
    {path : 'login', component: AuthentificationComponent}
];

@NgModule({
    imports: [ RouterModule.forRoot(routes) ],
    exports: [ RouterModule ]
  })
  export class AppRoutingModule {

  }
