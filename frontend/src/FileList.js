import { faFolder } from "@fortawesome/free-regular-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { useEffect, useState } from "react";
import { Breadcrumb, BreadcrumbItem, Button, Form, Table } from "react-bootstrap";
import { useParams } from "react-router";
import { Link } from "react-router-dom";

function FilePath({ path }) {

    return (
        <Breadcrumb>
            <BreadcrumbItem linkAs={Link} linkProps={{ to: "/" }} active={path.length === 0}>Root</BreadcrumbItem>
            {path.map((folder, index) => (
                <BreadcrumbItem linkAs={Link} linkProps={{ to: "/" + folder.id }} active={path.length === (index + 1)}>
                    {folder.name}
                </BreadcrumbItem>
            ))}
        </Breadcrumb>
    );
}

export default function FileList() {

    // when destructing, the destructed value doesn't show up in useEffect (don't know why)
    const params = useParams();

    const [isLoaded, setIsLoaded] = useState(false);
    const [folders, setFolders] = useState([]);
    const [files, setFiles] = useState([]);
    const [path, setPath] = useState([]);
    const [error, setError] = useState(null);

    const [newFolderName, setNewFolderName] = useState("");

    useEffect(() => {

        const { folderId } = params;
        const uri = "/api/files/" + (folderId !== undefined ? folderId : "");

        fetch(uri)
            .then(result => result.json())
            .then(
                result => {
                    const { files, path } = result;

                    setIsLoaded(true);
                    setFolders(files.filter(file => file.folder));
                    setFiles(files.filter(file => !file.folder));
                    console.log(result);
                    setPath(path);
                },
                error => {
                    setIsLoaded(true)
                    setError(error);
                });
    }, [params]);

    const createFolder = (event) => {
        event.preventDefault();

        const requestOptions = {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ name: newFolderName })
        };
        fetch('/api/files', requestOptions)
            .then(response => response.json())
            .then(data => console.log(data));

        console.log("creating folder,  " + newFolderName);
    }

    if (error) {
        return <div>Error: ${error.message}</div>;
    } else if (!isLoaded) {
        return <div>Loading ...</div>;
    } else {
        return (
            <>
                <FilePath path={[...path]} />
                <Form inline className="mb-3" onSubmit={createFolder}>
                    <Form.Control placeholder="Folder name..." onChange={e => setNewFolderName(e.target.value)} />
                    < Button variant="primary" type="submit" className="ml-3">Create</Button>
                </Form>
                <Table striped bordered hover>
                    <thead>
                        <tr>
                            <th></th>
                            <th>Name</th>
                        </tr>
                    </thead>
                    <tbody>
                        {folders.map(folder => (
                            <tr key={folder.id}>
                                <td><FontAwesomeIcon icon={faFolder} /></td>
                                <td><Link to={"/" + folder.id}>{folder.name}</Link></td>
                            </tr>
                        ))}
                        {files.map(file => (
                            <tr key={file.id}>
                                <td></td>
                                <td>{file.name}</td>
                            </tr>
                        ))}
                    </tbody>
                </Table>
            </>
        );
    }
}
