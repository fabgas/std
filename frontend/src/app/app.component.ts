import { Component } from '@angular/core';
import { AuthentificationService } from './components/services/authentification.service';
import { Router } from '../../node_modules/@angular/router';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'Mon jardin';
  constructor(private app: AuthentificationService,  private router: Router) {
  }
  public logout() {
    this.app.logout(() => {
    this.router.navigateByUrl('/login');
    });
  }
}
