package ru.otus.springboot.service;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import ru.otus.springboot.model.Address;
import ru.otus.springboot.model.Client;
import ru.otus.springboot.model.Phone;
import ru.otus.springboot.repository.AddressRepository;
import ru.otus.springboot.repository.ClientRepository;

@Service
@Transactional
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;
    private final AddressRepository addressRepository;

    public ClientServiceImpl(ClientRepository clientRepository, AddressRepository addressRepository) {
        this.clientRepository = clientRepository;
        this.addressRepository = addressRepository;
    }

    public Client saveClient(Client client) {
        Address saveAddress = addressRepository.save(client.getAddress());
        client.setAddressId(saveAddress.getId());
        Client saveClient = clientRepository.save(client);

        return saveClient;
    }

    public Client saveClientWithStreet(String name, String street) {
        Address address = new Address();
        address.setStreet(street);
        Address savedAddress = addressRepository.save(address);

        Client client = new Client();
        client.setName(name);
        client.setAddressId(savedAddress.getId());
        Client savedClient = clientRepository.save(client);

        savedClient.setAddress(savedAddress);

        return savedClient;
    }

    public Client saveClientWithAddressAndPhones(String name, String street, List<String> phoneNumbers) {

        Address savedAddress = null;
        if (street != null && !street.isEmpty()) {
            Address address = new Address();
            address.setStreet(street);
            savedAddress = addressRepository.save(address);
        }

        Client client = new Client();
        client.setName(name);
        if (savedAddress != null) {
            client.setAddressId(savedAddress.getId());
        }
        Client savedClient = clientRepository.save(client);

        if (phoneNumbers != null && !phoneNumbers.isEmpty()) {
            for (String number : phoneNumbers) {
                if (number != null && !number.trim().isEmpty()) {
                    Phone phone = new Phone();
                    phone.setNumber(number);
                    phone.setClientId(savedClient.getId());
                    savedClient.addPhone(phone);
                }
            }
            savedClient = clientRepository.save(savedClient);
        }

        if (savedAddress != null) {
            savedClient.setAddress(savedAddress);
        }

        return savedClient;
    }

    @Override
    public Optional<Client> findClient(long id) {
        Optional<Client> clientOptional = clientRepository.findById(id);

        if (clientOptional.isPresent()) {
            Client client = clientOptional.get();
            Long addressId = client.getAddressId();

            if (addressId != null) {
                addressRepository.findById(addressId).ifPresent(client::setAddress);
            }

            return Optional.of(client);
        }

        return Optional.empty();
    }

    @Override
    public List<Client> findAllClients() {
        List<Client> clients = clientRepository.findAll();

        for (Client client : clients) {
            if (client.getAddressId() != null) {
                Address address =
                        addressRepository.findById(client.getAddressId()).orElse(null);
                client.setAddress(address);
            }
        }

        return clients;
    }

    @Override
    public void deleteClient(long id) {
        Client client = clientRepository.findById(id).orElseThrow();

        if (client.getAddressId() != null) {
            addressRepository.deleteById(client.getAddressId());
        }

        clientRepository.delete(client);
    }

    @Override
    public Client updateClient(long id, Client client) {
        Client clientForId = clientRepository.findById(id).orElseThrow();
        Address addressForId =
                addressRepository.findById(clientForId.getAddressId()).orElse(null);
        Address address = client.getAddress();
        address.setId(addressForId.getId());
        Address saveAddress = addressRepository.save(address);
        clientForId.setAddressId(saveAddress.getId());
        clientForId.setName(client.getName());
        clientForId.setPhones(client.getPhones());
        Client saveClient = clientRepository.save(clientForId);
        addressRepository.findById(addressForId.getId()).ifPresent(saveClient::setAddress);

        return saveClient;
    }
}
