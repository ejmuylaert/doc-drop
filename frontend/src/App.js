import { BrowserRouter, Redirect, Route } from "react-router-dom";
import "./App.css";
import FileList from "./FileList";

function App() {
  return (
    <BrowserRouter>
      <Route path="/ui/:folderId">
        <FileList />
      </Route>
      <Route exact path="/ui">
        <FileList />
      </Route>
      <Route exact path="/">
        <Redirect to="/ui" />
      </Route>
    </BrowserRouter>
  );
}

export default App;
