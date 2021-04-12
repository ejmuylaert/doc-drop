package org.ej.docdrop.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.RemoteFile;
import net.schmizz.sshj.sftp.RemoteResourceInfo;
import net.schmizz.sshj.sftp.SFTPClient;
import org.ej.docdrop.domain.RemarkableDocument;
import org.ej.docdrop.domain.RemarkableFileTree;
import org.ej.docdrop.domain.RemarkableMetadata;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/browse")
public class RemarkableBrowserController {

    @GetMapping("")
    String index(Model model) throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        final SSHClient ssh = new SSHClient();
        ssh.loadKnownHosts();
        ssh.connect("192.168.2.5");
        ssh.authPassword("root", "ZjZQdup7xQ");
        SFTPClient client = ssh.newSFTPClient();
        List<RemoteResourceInfo> ls = client.ls("/home/root/.local/share/remarkable/xochitl");


        RemarkableFileTree tree = new RemarkableFileTree();
        for (RemoteResourceInfo info : ls) {
            if (fileExtension(info.getName()).equals("metadata")) {
                String contents = fileContents(client, info.getPath());
                RemarkableMetadata metadata = mapper.readValue(contents,
                        RemarkableMetadata.class);

                UUID id = UUID.fromString(baseName(info.getName()));
                tree.addDocument(new RemarkableDocument(id, metadata));
            }
        }

        client.close();
        ssh.close();

        model.addAttribute("documents", tree.documentsWithParent(null));

        return "remarkable/index";
    }

    static String fileContents(SFTPClient client, String fileName) throws IOException {
        RemoteFile remoteFile = client.open(fileName);

        int length = (int) remoteFile.length();
        byte[] contents = new byte[length];
        remoteFile.read(0, contents, 0, length);

        remoteFile.close();

        return new String(contents, StandardCharsets.UTF_8);
    }

    static String fileExtension(String fileName) {
        int i = fileName.lastIndexOf('.');

        if (i == -1) {
            return "";
        } else {
            return fileName.substring(i + 1);
        }
    }

    static String baseName(String fileName) {
        int i = fileName.lastIndexOf('.');

        if (i == -1) {
            return fileName;
        } else {
            return fileName.substring(0, i);
        }
    }
}