import sys
import getpass
import re
import atom
import gdata.contacts
import gdata.contacts.service
import config

MAX_CONTACTS = 2000
MAX_GROUPS = 100

class GroupExplorer(object):
    def __init__(self, username, password):
        self.username = username
        self.gd_client = gdata.contacts.service.ContactsService()
        self.gd_client.email = self.username
        self.gd_client.password = password
        self.gd_client.source = 'wanderlist-0.1'
        self.gd_client.ProgrammaticLogin()

        self.contacts_dict = {}
        self.fill_contacts_dict()
        self.groups_dict = {}
        self.fill_groups_dict()

        self.groups_to_include = {}
        self.groups_to_exclude = {}

    def fill_contacts_dict(self):
        query = gdata.contacts.service.ContactsQuery()
        query.max_results = MAX_CONTACTS
        feed = self.gd_client.GetContactsFeed(query.ToUri())
        for entry in feed.entry:
            self.contacts_dict[entry.id.text] = entry

    def fill_groups_dict(self):
        query = gdata.service.Query(feed='/m8/feeds/groups/default/full')
        query.max_results = MAX_GROUPS
        #feed = self.gd_client.GetGroupsFeed('%s&max-results=%d' % (query.ToUri(), MAX_GROUPS))
        feed = self.gd_client.GetGroupsFeed(query.ToUri())
        for entry in feed.entry:
            self.groups_dict[entry.content.text] = entry.id.text

    def print_all_groups(self):
        print '%s has the following groups:' % self.username
        for group in self.groups_dict.keys():
            print '  %s' % (group)

    def set_calculator(self, string):
        string = string.replace(' ', '')
        single_pat = re.compile('(\w+)')
        if single_pat.match(string):
            return self.get_contacts_for_group(string)
        else:
            return self.recursive_set_calculator(string)

    def recursive_set_calculator(self, string):
        op = '[|&-^]'
        n_pat = re.compile(('\A\( (    \w+     )  (%s)  (    \w+     )  \)\Z' % (op        )).replace(' ', ''))
        l_pat = re.compile(('\A\( (\(.+ %s .+\))  (%s)  (    \w+     )  \)\Z' % (op, op    )).replace(' ', ''))
        r_pat = re.compile(('\A\( (    \w+     )  (%s)  (\(.+ %s .+\))  \)\Z' % (op, op    )).replace(' ', ''))
        b_pat = re.compile(('\A\( (\(.+ %s .+\))  (%s)  (\(.+ %s .+\))  \)\Z' % (op, op, op)).replace(' ', ''))
        groups = None
        if   n_pat.match(string):
            groups = n_pat.match(string).groups()
            print 'none -> ' + string + ' -> ' + str(groups)
            l_set = self.get_contacts_for_group(groups[0])
            r_set = self.get_contacts_for_group(groups[2])
        elif l_pat.match(string):
            groups = l_pat.match(string).groups()
            print 'left -> ' + string + ' -> ' + str(groups)
            l_set = self.recursive_set_calculator(groups[0])
            r_set = self.get_contacts_for_group(groups[2])
        elif r_pat.match(string):
            groups = r_pat.match(string).groups()
            print 'right -> ' + string + ' -> ' + str(groups)
            l_set = self.get_contacts_for_group(groups[0])
            r_set = self.recursive_set_calculator(groups[2])
        elif b_pat.match(string):
            groups = b_pat.match(string).groups()
            print 'both -> ' + string + ' -> ' + str(groups)
            l_set = self.recursive_set_calculator(groups[0])
            r_set = self.recursive_set_calculator(groups[2])
        if groups == None:
            self.print_directions()
            self.print_all_groups()
            return set([])
        elif groups[1] == '|':
            return l_set.union(r_set)
        elif groups[1] == '&':
            return l_set.intersection(r_set)
        elif groups[1] == '-':
            return l_set.difference(r_set)
        elif groups[1] == '^':
            return l_set.symmetric_difference(r_set)

    def get_contacts_for_group(self, group_name):
        contacts = set([])
        query = gdata.contacts.service.ContactsQuery(group=self.groups_dict[group_name])
        query.max_results = MAX_CONTACTS
        feed = self.gd_client.GetContactsFeed(query.ToUri())
        for entry in feed.entry:
            contacts.add(entry.id.text)
        print '%4d contacts from group with name "%s" ' % (len(contacts) ,group_name)
        return contacts

    def print_contacts_in_set(self, result_set):
        for i, contact_id in enumerate(result_set):
            entry = self.contacts_dict[contact_id]
            print '%s %s' % (i+1, entry.title.text)
            #print '%s %s (%s)' % (i+1, entry.title.text, entry.id.text)
            # print '    %s' % (entry.id.text)
            # for email in entry.email:
            #     print '    %s' % (email.address)

    def print_directions(self):
        print '\n'
        print "List the set operations desired as pairs of group names separated by |, &, -, or ^ and"
        print "with each pair enclosed in exactly one single set of parenthesis."
        print '\n'

def main():
    password = config.password
    if password == '': password = getpass.getpass()
    explorer = GroupExplorer('lehrburger', password)

    try:
        string = sys.argv[1]
        string = '(%s - notpeople)' % string
        string = '(%s - unknown)' % string
        result_set = explorer.set_calculator(string)
        #result_set = explorer.set_calculator(sys.argv[1])
        print '\n'
        explorer.print_contacts_in_set(result_set)
    except IndexError:
        explorer.print_directions()
        explorer.print_all_groups()

if __name__ == '__main__':
    main()

