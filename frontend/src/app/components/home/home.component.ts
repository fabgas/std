import { Component, OnInit } from '@angular/core';
import { AuthentificationService } from '../services/authentification.service';
import { HttpClient } from '../../../../node_modules/@angular/common/http';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit {

  greeting ;

  constructor(private app: AuthentificationService, private http: HttpClient) {
    http.get('resource').subscribe(data => this.greeting = data);
  }

  ngOnInit() {
  }

  authenticated() { return this.app.authenticated; }
}
