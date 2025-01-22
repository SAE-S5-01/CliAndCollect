package fr.iutrodez.sae501.cliandcollect.clientUtils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import fr.iutrodez.sae501.cliandcollect.R;

public class ClientAdapter extends RecyclerView.Adapter<ClientHolder> {

    private List<Client> clients;
    public ClientAdapter(List<Client> donnees){
        clients = donnees;
    }

    @Override
    public ClientHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).
                inflate(R.layout.liste_client,viewGroup,false);
        return new ClientHolder(view);
    }

    @Override
    public void onBindViewHolder(ClientHolder holder, int position) {
        Client myClient = clients.get(position);
        holder.bind(myClient);
    }

    @Override
    public int getItemCount() {
        return clients.size();
    }
}
