# Digitalpanda

This project was generated with [Angular CLI](https://github.com/angular/angular-cli) version 7.0.6.

## Development environment setup
```
sudo apt-get update
sudo apt-get install nodejs-dev node-gyp libssl1.0-dev
sudo apt-get install npm
#old: sudo apt-get install build-essential checkinstall libssl-dev npm
curl -o- https://raw.githubusercontent.com/creationix/nvm/v0.32.1/install.sh | bash
nvm install 10.13.0
nvm use 10.13.0
nvm alias default
```

To run angular-cli commands with npm use `npm run <ng "command" -- --param1=value1>`

## Development server

Run `ng serve` for a dev server. Navigate to `http://localhost:4200/`. The app will automatically reload if you change any of the source files.

## Code scaffolding

Run `ng generate component component-name` to generate a new component. You can also use `ng generate directive|pipe|service|class|guard|interface|enum|module`.

## Build

Run `ng build` to build the project. The build artifacts will be stored in the `dist/` directory. Use the `--prod` flag for a production build.

To serve the resulting build, run `http-server -a 0.0.0.0 -p 8000 $(pwd)/dist/digitalpanda`
Precondition: nodeJS http-server is installed : `npm install http-server -g`

## Running unit tests

Run `export CHROME_BIN=/snap/bin/chromium; ng test` to execute the unit tests via [Karma](https://karma-runner.github.io).

## Running end-to-end tests

Run `ng e2e` to execute the end-to-end tests via [Protractor](http://www.protractortest.org/).

## Further help

To get more help on the Angular CLI use `ng help` or go check out the [Angular CLI README](https://github.com/angular/angular-cli/blob/master/README.md).
