
/* These are JavaScript import statements. Angular doesn’t know anything about these. */
import { NgModule  } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BrowserModule } from '@angular/platform-browser';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { RouterModule } from '@angular/router';

import { NavBarComponent } from './navbar/navbar.component';
import { PageTitleComponent } from './page-title/page-title.component';

@NgModule({
  imports: [RouterModule, CommonModule, BrowserModule, NgbModule],
  providers: [],
  declarations: [
    NavBarComponent, PageTitleComponent
  ],
  exports: [
    NavBarComponent, PageTitleComponent
  ]
})
export class UiModule { }
