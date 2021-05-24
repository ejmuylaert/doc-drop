import { BrowserRouter, Route } from "react-router-dom";
import "./App.css";
import FileList from "./FileList";

function App() {
  return (
    <BrowserRouter>
      <Route path="/:folderId">
        <FileList />
      </Route>
      <Route exact path="/">
        <FileList />
      </Route>
    </BrowserRouter>
  );
}

export default App;
